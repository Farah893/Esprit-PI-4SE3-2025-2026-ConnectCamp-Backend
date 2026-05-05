package tn.esprit.projetintegre.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationRequest {
    private Long productId;
    private Long variantId;       // Optional: specific product variant
    private String countryCode;   // ISO 2-letter code (FR, TN, US, …)
    private String postalCode;    // Optional: for fine-grained shipping zones
    private Integer quantity;
    private String currency;// Optional: defaults to 1
}
