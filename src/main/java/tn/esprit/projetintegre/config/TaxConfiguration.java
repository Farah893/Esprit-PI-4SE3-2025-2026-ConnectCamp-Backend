package tn.esprit.projetintegre.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "pricing")
public class TaxConfiguration {

    /** Minimum order value (after discount) for free shipping */
    private BigDecimal freeShippingThreshold = new BigDecimal("100.00");

    /** Base shipping costs by weight bracket */
    private BigDecimal shippingSmall  = new BigDecimal("5.99");   // weight <= 0.5 kg
    private BigDecimal shippingMedium = new BigDecimal("9.99");   // weight <= 2.0 kg
    private BigDecimal shippingLarge  = new BigDecimal("19.99");  // weight <= 10.0 kg
    private BigDecimal shippingXLarge = new BigDecimal("39.99");  // weight >  10.0 kg

    /** Average customs duty rate applied to non-EU shipments exceeding the threshold */
    private BigDecimal customsRate = new BigDecimal("5.0");

    /** Order value threshold (EUR) above which customs fees apply */
    private BigDecimal customsThreshold = new BigDecimal("150.00");

    /** Fallback weight (kg) when a product has no weight set */
    private Double defaultWeight = 1.0;

    /**
     * Delivery estimates in business days per shipping zone.
     * Key   = ShippingZone name (e.g. "DOMESTIC", "EU", …)
     * Value = int[]{minDays, maxDays}
     */
    private Map<String, int[]> deliveryEstimates = new HashMap<>();

    public TaxConfiguration() {
        deliveryEstimates.put("DOMESTIC",      new int[]{2,  3});
        deliveryEstimates.put("EU",            new int[]{5,  8});
        deliveryEstimates.put("MAGHREB",       new int[]{7,  12});
        deliveryEstimates.put("MIDDLE_EAST",   new int[]{7,  14});
        deliveryEstimates.put("NORTH_AMERICA", new int[]{10, 15});
        deliveryEstimates.put("ASIA",          new int[]{10, 20});
        deliveryEstimates.put("OTHER",         new int[]{15, 30});
    }

    // ----------------------------------------------------------------
    // Getters & Setters (required by @ConfigurationProperties)
    // ----------------------------------------------------------------

    public BigDecimal getFreeShippingThreshold() { return freeShippingThreshold; }
    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public BigDecimal getShippingSmall() { return shippingSmall; }
    public void setShippingSmall(BigDecimal shippingSmall) { this.shippingSmall = shippingSmall; }

    public BigDecimal getShippingMedium() { return shippingMedium; }
    public void setShippingMedium(BigDecimal shippingMedium) { this.shippingMedium = shippingMedium; }

    public BigDecimal getShippingLarge() { return shippingLarge; }
    public void setShippingLarge(BigDecimal shippingLarge) { this.shippingLarge = shippingLarge; }

    public BigDecimal getShippingXLarge() { return shippingXLarge; }
    public void setShippingXLarge(BigDecimal shippingXLarge) { this.shippingXLarge = shippingXLarge; }

    public BigDecimal getCustomsRate() { return customsRate; }
    public void setCustomsRate(BigDecimal customsRate) { this.customsRate = customsRate; }

    public BigDecimal getCustomsThreshold() { return customsThreshold; }
    public void setCustomsThreshold(BigDecimal customsThreshold) { this.customsThreshold = customsThreshold; }

    public Double getDefaultWeight() { return defaultWeight; }
    public void setDefaultWeight(Double defaultWeight) { this.defaultWeight = defaultWeight; }

    public Map<String, int[]> getDeliveryEstimates() { return deliveryEstimates; }
    public void setDeliveryEstimates(Map<String, int[]> deliveryEstimates) {
        this.deliveryEstimates = deliveryEstimates;
    }
}
