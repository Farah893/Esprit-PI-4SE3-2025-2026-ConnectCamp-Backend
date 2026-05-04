package tn.esprit.projetintegre.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.projetintegre.dto.EonetEventDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Service qui interroge l'API NASA EONET v3 pour récupérer les événements
 * naturels actifs à proximité d'une position géographique donnée.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NasaEonetService {

    private static final String EONET_URL =
            "https://eonet.gsfc.nasa.gov/api/v3/events?status=open&limit=50&bbox={minLon},{minLat},{maxLon},{maxLat}";

    /** Rayon de recherche en degrés (~111 km par degré) */
    private static final double RADIUS_DEG = 5.0; // ~555 km autour de l'utilisateur

    private final RestTemplate restTemplate;

    /**
     * Retourne les événements EONET actifs dans un rayon autour de la position.
     */
    public List<EonetEventDTO> getEventsNear(double latitude, double longitude) {
        double minLat = latitude  - RADIUS_DEG;
        double maxLat = latitude  + RADIUS_DEG;
        double minLon = longitude - RADIUS_DEG;
        double maxLon = longitude + RADIUS_DEG;

        String url = EONET_URL
                .replace("{minLon}", String.valueOf(minLon))
                .replace("{minLat}", String.valueOf(minLat))
                .replace("{maxLon}", String.valueOf(maxLon))
                .replace("{maxLat}", String.valueOf(maxLat));

        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode body = response.getBody();
            if (body == null || !body.has("events")) return List.of();

            List<EonetEventDTO> events = new ArrayList<>();
            for (JsonNode event : body.get("events")) {
                String id       = event.path("id").asText();
                String title    = event.path("title").asText();
                String category = "";
                if (event.has("categories") && event.get("categories").isArray()
                        && !event.get("categories").isEmpty()) {
                    category = event.get("categories").get(0).path("title").asText();
                }

                double evtLat = Double.NaN;
                double evtLon = Double.NaN;
                if (event.has("geometry") && event.get("geometry").isArray()
                        && !event.get("geometry").isEmpty()) {
                    JsonNode geom  = event.get("geometry").get(0);
                    JsonNode coord = geom.path("coordinates");
                    if (coord.isArray() && coord.size() >= 2) {
                        evtLon = coord.get(0).asDouble();
                        evtLat = coord.get(1).asDouble();
                    }
                }

                events.add(new EonetEventDTO(id, title, category, evtLat, evtLon));
            }
            log.info("NASA EONET: {} event(s) found near ({}, {})", events.size(), latitude, longitude);
            return events;

        } catch (Exception e) {
            log.warn("NASA EONET API call failed: {}", e.getMessage());
            return List.of();
        }
    }

    public List<String> getEventTitlesNear(double latitude, double longitude) {
        return getEventsNear(latitude, longitude)
                .stream()
                .map(EonetEventDTO::getTitle)
                .collect(java.util.stream.Collectors.toList());
    }
}
