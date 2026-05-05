package tn.esprit.projetintegre.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "carriers")
public class CarrierApiConfiguration {

    /** Templates d'URL de suivi — {trackingNumber} sera remplacé dynamiquement */
    private Map<String, String> trackingUrls = new HashMap<>();

    /** Sites web officiels des transporteurs */
    private Map<String, String> carrierWebsites = new HashMap<>();

    /** Clés API pour intégration future (V2) */
    private Map<String, String> apiKeys = new HashMap<>();

    /** Activer l'intégration API réelle (false = mode manuel V1) */
    private Boolean enableApiIntegration = false;

    /** Transporteur par défaut si non renseigné sur la commande */
    private String defaultCarrier = "Colissimo";

    public CarrierApiConfiguration() {
        // Tracking URLs
        trackingUrls.put("LaPoste",
                "https://www.laposte.fr/outils/suivre-vos-envois?code={trackingNumber}");
        trackingUrls.put("Colissimo",
                "https://www.laposte.fr/outils/suivre-vos-envois?code={trackingNumber}");
        trackingUrls.put("DHL",
                "https://www.dhl.fr/fr/express/suivi.html?AWB={trackingNumber}");
        trackingUrls.put("FedEx",
                "https://www.fedex.com/fedextrack/?trknbr={trackingNumber}");
        trackingUrls.put("UPS",
                "https://www.ups.com/track?loc=fr_FR&tracknum={trackingNumber}");
        trackingUrls.put("Chronopost",
                "https://www.chronopost.fr/tracking-no-cms/suivi-page?listeNumerosLT={trackingNumber}");
        trackingUrls.put("GLS",
                "https://gls-group.eu/track/{trackingNumber}");
        trackingUrls.put("TNT",
                "https://www.tnt.fr/express/fr_fr/site/shipping/tracking.html?searchType=con&cons={trackingNumber}");

        // Carrier websites
        carrierWebsites.put("LaPoste", "https://www.laposte.fr");
        carrierWebsites.put("Colissimo", "https://www.laposte.fr/particulier");
        carrierWebsites.put("DHL", "https://www.dhl.fr");
        carrierWebsites.put("FedEx", "https://www.fedex.com/fr-fr");
        carrierWebsites.put("UPS", "https://www.ups.com/fr");
        carrierWebsites.put("Chronopost", "https://www.chronopost.fr");
        carrierWebsites.put("GLS", "https://gls-group.eu/fr");
        carrierWebsites.put("TNT", "https://www.tnt.fr");
    }

    /**
     * Génère l'URL de suivi complète pour un transporteur et un numéro de colis.
     * Utilise le transporteur par défaut si le transporteur est inconnu.
     */
    public String getTrackingUrl(String carrier, String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isBlank()) return null;
        String template = trackingUrls.getOrDefault(
                normalizeCarrier(carrier),
                trackingUrls.get(defaultCarrier)
        );
        if (template == null) return null;
        return template.replace("{trackingNumber}", trackingNumber);
    }

    /**
     * Retourne le site web officiel d'un transporteur.
     */
    public String getCarrierWebsite(String carrier) {
        return carrierWebsites.getOrDefault(
                normalizeCarrier(carrier),
                carrierWebsites.getOrDefault(defaultCarrier, "")
        );
    }

    /** Normalise le nom du transporteur (casse insensible) */
    private String normalizeCarrier(String carrier) {
        if (carrier == null || carrier.isBlank()) return defaultCarrier;
        // Essai direct
        if (trackingUrls.containsKey(carrier)) return carrier;
        // Essai avec capitalisation
        String capitalized = carrier.substring(0, 1).toUpperCase() + carrier.substring(1).toLowerCase();
        if (trackingUrls.containsKey(capitalized)) return capitalized;
        return defaultCarrier;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public Map<String, String> getTrackingUrls() { return trackingUrls; }
    public void setTrackingUrls(Map<String, String> trackingUrls) { this.trackingUrls = trackingUrls; }

    public Map<String, String> getCarrierWebsites() { return carrierWebsites; }
    public void setCarrierWebsites(Map<String, String> carrierWebsites) { this.carrierWebsites = carrierWebsites; }

    public Map<String, String> getApiKeys() { return apiKeys; }
    public void setApiKeys(Map<String, String> apiKeys) { this.apiKeys = apiKeys; }

    public Boolean getEnableApiIntegration() { return enableApiIntegration; }
    public void setEnableApiIntegration(Boolean enableApiIntegration) { this.enableApiIntegration = enableApiIntegration; }

    public String getDefaultCarrier() { return defaultCarrier; }
    public void setDefaultCarrier(String defaultCarrier) { this.defaultCarrier = defaultCarrier; }
}
