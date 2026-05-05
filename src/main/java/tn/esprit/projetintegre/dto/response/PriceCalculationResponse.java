package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationResponse {
    private Long productId;
    private String productName;
    private Long variantId;
    private String requestedCountry;
    private String requestedPostalCode;

    // Pricing breakdown
    private BigDecimal basePrice;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private BigDecimal priceAfterDiscount;

    // Tax info
    private TaxBreakdown tax;

    // Customs
    private BigDecimal customsFees;

    // Shipping info
    private ShippingBreakdown shipping;

    // Totals
    private BigDecimal subtotalHT;    // Price after discount, before tax
    private BigDecimal totalTax;
    private BigDecimal totalShipping;
    private BigDecimal totalCustoms;
    private BigDecimal totalPrice;    // Final TTC (all-inclusive)

    // Availability
    private Boolean inStock;
    private Integer availableQuantity;

    // Metadata
    private String currency;
    private Instant calculatedAt;
}
