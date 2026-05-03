package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.projetintegre.enums.ShippingZone;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ShippingBreakdown {
    private ShippingZone zone;
    private BigDecimal multiplier;
    private BigDecimal baseCost;
    private BigDecimal finalCost;
    private Boolean isFree;
    private String freeShippingReason;
    private String estimatedDeliveryMin;  // ISO date string e.g. "2026-05-05"
    private String estimatedDeliveryMax;  // ISO date string e.g. "2026-05-08"
}