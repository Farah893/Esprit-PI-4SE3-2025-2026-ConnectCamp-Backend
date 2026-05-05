package tn.esprit.projetintegre.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {
    private Long id;
    private String code;
    private String description;
    private String discountType; // Doit correspondre à type.name() dans le mapper
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount; // Changé de minPurchaseAmount
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit; // Changé de maxUsage
    private Integer currentUsage; // Doit correspondre à usageCount
    private Integer usageLimitPerUser;
    private LocalDateTime validFrom; // Changé de startDate
    private LocalDateTime validUntil; // Changé de endDate
    private Boolean isActive;
    private Boolean isValid;
    private Long applicableCategoryId; // Ajouté
    private LocalDateTime createdAt;
}
