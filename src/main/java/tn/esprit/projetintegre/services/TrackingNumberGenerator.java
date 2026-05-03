package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.projetintegre.entities.Order;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Génère un numéro de tracking unique et l'enregistre sur 17TRACK.
 * Après appel → numéro visible sur dashboard.17track.net
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingNumberGenerator {

    private final TrackingApiService trackingApiService;

    /**
     * Génère un numéro format reconnu par 17TRACK :
     *   Colissimo → CC + 9 chiffres + FR  (ex: CC847392015FR)
     *   DHL       → DH + 9 chiffres + FR
     *   FedEx     → FX + 9 chiffres + FR
     */
    public String generateTrackingNumber(String carrierName, String countryCode) {
        String prefix = switch (carrierName != null ? carrierName.toUpperCase() : "") {
            case "LAPOSTE", "COLISSIMO" -> "CC";
            case "DHL"                  -> "DH";
            case "FEDEX"                -> "FX";
            case "UPS"                  -> "UP";
            case "CHRONOPOST"           -> "CX";
            default                     -> "CC";
        };

        String country = (countryCode != null && countryCode.length() >= 2)
                ? countryCode.substring(0, 2).toUpperCase()
                : "FR";

        long randomPart = ThreadLocalRandom.current().nextLong(100_000_000L, 999_999_999L);
        return prefix + randomPart + country;
    }

    /**
     * Génère le numéro ET l'enregistre sur 17TRACK.
     * Après cet appel → numéro visible sur dashboard.17track.net
     *
     * @param order       Commande créée
     * @param carrierName Nom transporteur (ex: "Colissimo")
     * @param countryCode Code pays destination (ex: "FR", "TN")
     * @return            Numéro de tracking généré
     */
    public String generateAndRegisterTracking(Order order, String carrierName, String countryCode) {

        String trackingNumber = generateTrackingNumber(carrierName, countryCode);

        log.info("═══════════════════════════════════════════════");
        log.info("📦 Tracking généré : {}", trackingNumber);
        log.info("   Commande       : #{} ({})", order.getId(), order.getOrderNumber());
        log.info("   Transporteur   : {}", carrierName);
        log.info("   Destination    : {}", countryCode);

        try {
            String response = trackingApiService.registerTracking(trackingNumber);

            if (response != null && response.contains("-18019901")) {
                log.info("ℹ️  {} déjà enregistré sur 17TRACK (OK)", trackingNumber);
            } else if (response != null && response.contains("\"accepted\"")) {
                log.info("✅ {} enregistré → visible sur dashboard.17track.net", trackingNumber);
            } else {
                log.warn("⚠️  Réponse 17TRACK inattendue: {}", response);
            }
        } catch (Exception e) {
            log.error("❌ Erreur 17TRACK register (numéro conservé quand même): {}", e.getMessage());
        }

        log.info("═══════════════════════════════════════════════");
        return trackingNumber;
    }
}