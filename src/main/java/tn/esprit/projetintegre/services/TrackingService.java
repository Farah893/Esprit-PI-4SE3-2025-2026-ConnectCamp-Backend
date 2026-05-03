package tn.esprit.projetintegre.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.config.CarrierApiConfiguration;
import tn.esprit.projetintegre.dto.request.TrackingUpdateRequest;
import tn.esprit.projetintegre.dto.response.*;
import tn.esprit.projetintegre.entities.Order;
import tn.esprit.projetintegre.enums.OrderStatus;
import tn.esprit.projetintegre.enums.TrackingStatus;
import tn.esprit.projetintegre.repositories.OrderRepository;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final OrderRepository orderRepository;
    private final CarrierApiConfiguration carrierConfig;
    private final TrackingApiService trackingApiService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Formats de date retournés par 17TRACK (plusieurs formats possibles selon transporteur)
     */
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );

    private static final Map<String, Integer> DELIVERY_DAYS = Map.ofEntries(
            Map.entry("FR", 3), Map.entry("BE", 4), Map.entry("LU", 4),
            Map.entry("DE", 5), Map.entry("ES", 5), Map.entry("IT", 5),
            Map.entry("NL", 5), Map.entry("PT", 6), Map.entry("GB", 6),
            Map.entry("CH", 6), Map.entry("TN", 10), Map.entry("MA", 12),
            Map.entry("DZ", 12), Map.entry("US", 15), Map.entry("CA", 15),
            Map.entry("CN", 20), Map.entry("JP", 18)
    );

    // =========================================================
    // MÉTHODES PUBLIQUES
    // =========================================================

    public TrackingResponse getTracking(Long orderId) {
        Order order = findOrder(orderId);

        // Pas de numéro de tracking → fallback interne uniquement
        if (order.getTrackingNumber() == null || order.getTrackingNumber().isBlank()) {
            log.info("Commande #{} sans numéro tracking → timeline interne", orderId);
            return buildTrackingResponse(order, List.of());
        }

        try {
            // 1. Enregistrement du colis auprès de 17TRACK (idempotent)
            trackingApiService.registerTracking(order.getTrackingNumber());

            // 2. Récupération des événements
            String json = trackingApiService.getTrackingInfo(order.getTrackingNumber());

            if (json == null || json.isBlank()) {
                log.warn("17TRACK: réponse vide → fallback interne");
                return buildTrackingResponse(order, List.of());
            }

            // 3. Parsing avec la VRAIE structure JSON de 17TRACK
            List<TrackingEvent> events = parse17TrackResponse(json);

            if (events.isEmpty()) {
                log.info("17TRACK: 0 événements → fallback interne (colis peut-être pas encore scanné)");
                return buildTrackingResponse(order, List.of());
            }

            log.info("17TRACK: {} événements pour {}", events.size(), order.getTrackingNumber());
            return buildTrackingResponse(order, events);

        } catch (Exception e) {
            log.error("Erreur API 17TRACK → fallback interne: {}", e.getMessage(), e);
            return buildTrackingResponse(order, List.of());
        }
    }

    public TrackingResponse getTrackingByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Commande introuvable: " + orderNumber));
        return getTracking(order.getId());
    }

    @Transactional
    public TrackingResponse addTrackingEvent(Long orderId, TrackingUpdateRequest request) {
        Order order = findOrder(orderId);
        updateOrderStatusFromTracking(order, request.getStatus());
        updateOrderTimestamps(order, request.getStatus());
        orderRepository.save(order);
        log.info("Tracking manuel commande #{}: {}", orderId, request.getStatus());
        return buildTrackingResponse(order, List.of());
    }

    public List<TrackingResponse> getTrackingByUser(Long userId) {
        return orderRepository.findByUserId(userId, Pageable.unpaged()).getContent()
                .stream()
                .filter(o -> o.getTrackingNumber() != null || o.getShippedAt() != null)
                .map(o -> buildTrackingResponse(o, List.of()))
                .collect(Collectors.toList());
    }

    // =========================================================
    // PARSING JSON 17TRACK — STRUCTURE RÉELLE API v2.4
    // =========================================================
    // Structure réelle retournée:
    // {
    //   "code": 0,
    //   "data": {
    //     "accepted": [{
    //       "number": "SF1234567890",
    //       "track": {
    //         "w1": {
    //           "z0": { "a":"DELIVERED", "b":"2026-05-01 10:00:00", "c":"Paris", "d":"Livré" },
    //           "z1": [       ← LES ÉVÉNEMENTS SONT ICI (pas dans "events") !!
    //             { "a":"DELIVERED", "b":"2026-05-01 10:00:00", "c":"Paris",  "d":"Livré" },
    //             { "a":"INTRANSIT", "b":"2026-04-30 08:00:00", "c":"Lyon",   "d":"En transit" }
    //           ]
    //         }
    //       }
    //     }]
    //   }
    // }
    // Champs: "a"=status, "b"=timestamp, "c"=location, "d"=description
    // =========================================================

    private List<TrackingEvent> parse17TrackResponse(String json) {
        List<TrackingEvent> events = new ArrayList<>();

        try {
            JsonNode root = MAPPER.readTree(json);

            // Log le JSON complet pour debug
            log.debug("17TRACK JSON reçu: {}", json);

            int code = root.path("code").asInt(-1);
            if (code != 0) {
                log.warn("17TRACK: code={} (0=success). Message: {}", code, root.path("message").asText());
            }

            JsonNode accepted = root.path("data").path("accepted");
            if (accepted.isMissingNode() || !accepted.isArray() || accepted.isEmpty()) {
                // Vérifier rejected pour comprendre pourquoi
                JsonNode rejected = root.path("data").path("rejected");
                if (!rejected.isMissingNode() && rejected.isArray() && !rejected.isEmpty()) {
                    log.warn("17TRACK: colis REJETÉ: {}", rejected.get(0));
                }
                log.warn("17TRACK: data.accepted vide ou absent");
                return events;
            }

            JsonNode firstItem = accepted.get(0);
            log.debug("17TRACK accepted[0] keys: {}", firstItem.fieldNames());

            JsonNode track = firstItem.path("track");
            if (track.isMissingNode()) {
                log.warn("17TRACK: pas de nœud 'track' dans accepted[0]");
                return events;
            }

            // Chercher les événements dans w1.z1 (premier transporteur)
            // Si international, il peut y avoir w2.z1 (deuxième transporteur)
            JsonNode z1 = JsonNode.class.cast(track.path("w1").path("z1"));

            if (z1.isMissingNode() || !z1.isArray() || z1.isEmpty()) {
                // Essayer w2 (transit international)
                z1 = track.path("w2").path("z1");
            }

            if (z1.isMissingNode() || !z1.isArray() || z1.isEmpty()) {
                log.warn("17TRACK: aucun événement trouvé dans track.w1.z1 ni track.w2.z1");
                log.warn("17TRACK: contenu track = {}", track);
                return events;
            }

            log.info("17TRACK: parsing {} événements de z1", z1.size());

            for (JsonNode ev : z1) {
                String statusCode  = ev.path("a").asText("INTRANSIT");
                String timeStr     = ev.path("b").asText("");
                String location    = ev.path("c").asText("");
                String description = ev.path("d").asText("");

                LocalDateTime timestamp = parseDateTime(timeStr);
                if (timestamp == null) {
                    log.warn("17TRACK: date '{}' non parseable, utilisation de now", timeStr);
                    timestamp = LocalDateTime.now();
                }

                events.add(TrackingEvent.builder()
                        .timestamp(timestamp)
                        .status(mapStatusCode(statusCode))
                        .location(location)
                        .description(description)
                        .carrierStatus(statusCode)
                        .build());
            }

        } catch (Exception e) {
            log.error("17TRACK: erreur parsing: {}", e.getMessage(), e);
        }

        events.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return events;
    }

    /**
     * Mapping codes 17TRACK → notre enum TrackingStatus
     * Référence: https://www.17track.net/en/apicenter#docs-tracking-carrier-status
     */
    private TrackingStatus mapStatusCode(String code) {
        if (code == null) return TrackingStatus.IN_TRANSIT;
        return switch (code.toUpperCase().trim()) {
            case "DELIVERED"                       -> TrackingStatus.DELIVERED;
            case "INTRANSIT"                       -> TrackingStatus.IN_TRANSIT;
            case "OUTFORDELIVERY"                  -> TrackingStatus.OUT_FOR_DELIVERY;
            case "AVAILABLEFORPICKUP"              -> TrackingStatus.AT_PICKUP_POINT;
            case "PICKUP", "INFOSRECEIVED"         -> TrackingStatus.PICKED_UP;
            case "DELIVERYFAILURE", "FAILEDATTEMPT"-> TrackingStatus.DELIVERY_ATTEMPTED;
            case "EXCEPTION"                       -> TrackingStatus.EXCEPTION;
            case "RETURNEDTOSENDER","RETURNTOSENDER"-> TrackingStatus.RETURNED_TO_SENDER;
            case "NOTFOUND"                        -> TrackingStatus.NOT_SHIPPED;
            default -> {
                log.debug("17TRACK: code '{}' inconnu → IN_TRANSIT", code);
                yield TrackingStatus.IN_TRANSIT;
            }
        };
    }

    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try { return LocalDateTime.parse(s.trim(), fmt); }
            catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    // =========================================================
    // CONSTRUCTION RÉPONSE
    // =========================================================

    private TrackingResponse buildTrackingResponse(Order order, List<TrackingEvent> externalEvents) {
        List<TrackingEvent> events = externalEvents.isEmpty()
                ? buildInternalTimeline(order)
                : externalEvents;

        CurrentTrackingStatus current = events.isEmpty()
                ? getCurrentStatusFromOrder(order)
                : mapToCurrentStatus(events.get(0));

        String carrier = resolveCarrier(order.getCarrierName());

        return TrackingResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .trackingNumber(order.getTrackingNumber())
                .carrierName(carrier)
                .carrierWebsite(carrierConfig.getCarrierWebsite(carrier))
                .carrierTrackingUrl(carrierConfig.getTrackingUrl(carrier, order.getTrackingNumber()))
                .currentStatus(current)
                .estimatedDelivery(calculateEstimatedDelivery(order))
                .events(events)
                .shippingInfo(TrackingResponse.ShippingInfo.builder()
                        .recipientName(safe(order.getShippingName()))
                        .address(safe(order.getShippingAddress()))
                        .city(safe(order.getShippingCity()))
                        .postalCode(safe(order.getShippingPostalCode()))
                        .country(safe(order.getShippingCountry()))
                        .build())
                .lastUpdated(Instant.now())
                .build();
    }

    // =========================================================
    // TIMELINE INTERNE (FALLBACK)
    // =========================================================

    private List<TrackingEvent> buildInternalTimeline(Order order) {
        List<TrackingEvent> events = new ArrayList<>();

        if (order.getStatus() == OrderStatus.CANCELLED && order.getCancelledAt() != null)
            events.add(event(order.getCancelledAt(), TrackingStatus.EXCEPTION, "", "Commande annulée"));

        if (order.getDeliveredAt() != null)
            events.add(event(order.getDeliveredAt(), TrackingStatus.DELIVERED,
                    safe(order.getShippingCity()), "Colis livré à " + safe(order.getShippingName())));

        if (order.getShippedAt() != null)
            events.add(event(order.getShippedAt(), TrackingStatus.PICKED_UP,
                    "Centre de tri", "Pris en charge par " + resolveCarrier(order.getCarrierName())));

        LocalDateTime created = order.getOrderedAt() != null ? order.getOrderedAt() : order.getCreatedAt();
        if (created != null)
            events.add(event(created, TrackingStatus.LABEL_CREATED,
                    "ConnectCamp", "Commande confirmée — " + safe(order.getOrderNumber())));

        events.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return events;
    }

    private CurrentTrackingStatus getCurrentStatusFromOrder(Order order) {
        if (order.getDeliveredAt() != null)
            return statusDto(TrackingStatus.DELIVERED, order.getDeliveredAt(), safe(order.getShippingCity()), "Colis livré");
        if (order.getShippedAt() != null)
            return statusDto(TrackingStatus.IN_TRANSIT, order.getShippedAt(), "En transit", "En acheminement");
        if (order.getStatus() == OrderStatus.CANCELLED)
            return statusDto(TrackingStatus.EXCEPTION,
                    order.getCancelledAt() != null ? order.getCancelledAt() : order.getUpdatedAt(),
                    "", "Commande annulée");
        LocalDateTime ref = order.getOrderedAt() != null ? order.getOrderedAt() : order.getCreatedAt();
        return statusDto(TrackingStatus.NOT_SHIPPED, ref, "Entrepôt ConnectCamp", "En préparation");
    }

    private CurrentTrackingStatus mapToCurrentStatus(TrackingEvent e) {
        return CurrentTrackingStatus.builder()
                .code(e.getStatus()).label(e.getStatus().getLabel()).severity(e.getStatus().getSeverity())
                .timestamp(e.getTimestamp()).location(e.getLocation()).description(e.getDescription())
                .build();
    }

    // =========================================================
    // LIVRAISON ESTIMÉE
    // =========================================================

    private EstimatedDelivery calculateEstimatedDelivery(Order order) {
        if (order.getDeliveredAt() != null)
            return EstimatedDelivery.builder()
                    .minDate(order.getDeliveredAt().toLocalDate())
                    .maxDate(order.getDeliveredAt().toLocalDate())
                    .isExpired(false).daysRemaining(0).build();

        if (order.getStatus() == OrderStatus.CANCELLED)
            return EstimatedDelivery.builder().isExpired(true).daysRemaining(0).build();

        LocalDate base = order.getShippedAt() != null ? order.getShippedAt().toLocalDate()
                : (order.getOrderedAt() != null ? order.getOrderedAt().toLocalDate() : LocalDate.now());

        String country = order.getShippingCountry() != null ? order.getShippingCountry().toUpperCase() : "FR";
        int days = DELIVERY_DAYS.getOrDefault(country, 10);
        LocalDate estimated = addBusinessDays(base, days);
        long remaining = ChronoUnit.DAYS.between(LocalDate.now(), estimated);

        return EstimatedDelivery.builder()
                .minDate(addBusinessDays(base, Math.max(1, days - 1)))
                .maxDate(addBusinessDays(base, days + 2))
                .isExpired(remaining < 0)
                .daysRemaining((int) Math.max(0, remaining))
                .build();
    }

    private LocalDate addBusinessDays(LocalDate start, int days) {
        LocalDate r = start; int added = 0;
        while (added < days) {
            r = r.plusDays(1);
            if (r.getDayOfWeek() != DayOfWeek.SATURDAY && r.getDayOfWeek() != DayOfWeek.SUNDAY) added++;
        }
        return r;
    }

    // =========================================================
    // MISE À JOUR STATUT & TIMESTAMPS
    // =========================================================

    private void updateOrderStatusFromTracking(Order order, TrackingStatus s) {
        switch (s) {
            case PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, AT_PICKUP_POINT -> order.setStatus(OrderStatus.SHIPPED);
            case DELIVERED -> order.setStatus(OrderStatus.DELIVERED);
            case RETURNED_TO_SENDER, EXCEPTION -> order.setStatus(OrderStatus.CANCELLED);
            default -> {}
        }
    }

    private void updateOrderTimestamps(Order order, TrackingStatus s) {
        LocalDateTime now = LocalDateTime.now();
        switch (s) {
            case PICKED_UP -> { if (order.getShippedAt() == null) order.setShippedAt(now); }
            case DELIVERED -> {
                if (order.getDeliveredAt() == null) order.setDeliveredAt(now);
                if (order.getShippedAt() == null)   order.setShippedAt(now.minusDays(1));
            }
            case RETURNED_TO_SENDER, EXCEPTION -> { if (order.getCancelledAt() == null) order.setCancelledAt(now); }
            default -> {}
        }
    }

    // =========================================================
    // UTILITAIRES
    // =========================================================

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable: " + id));
    }
    private String resolveCarrier(String n) { return (n != null && !n.isBlank()) ? n : carrierConfig.getDefaultCarrier(); }
    private String safe(String s) { return s != null ? s : ""; }
    private TrackingEvent event(LocalDateTime ts, TrackingStatus st, String loc, String desc) {
        return TrackingEvent.builder().timestamp(ts).status(st).location(loc).description(desc).build();
    }
    private CurrentTrackingStatus statusDto(TrackingStatus st, LocalDateTime ts, String loc, String desc) {
        return CurrentTrackingStatus.builder().code(st).label(st.getLabel()).severity(st.getSeverity())
                .timestamp(ts).location(loc).description(desc).build();
    }
}