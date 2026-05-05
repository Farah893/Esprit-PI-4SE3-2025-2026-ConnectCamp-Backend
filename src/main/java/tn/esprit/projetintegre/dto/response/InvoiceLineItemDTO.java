package tn.esprit.projetintegre.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLineItemDTO {
    private String productName;
    private String productSku;
    private String productThumbnail;
    private Integer quantity;
    private String unit;
    private BigDecimal unitPriceHT;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal totalPriceHT;
    private BigDecimal totalPriceTTC;
}
