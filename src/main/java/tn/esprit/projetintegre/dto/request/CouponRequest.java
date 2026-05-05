package tn.esprit.projetintegre.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import tn.esprit.projetintegre.enums.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequest {
    @NotBlank(message = "Coupon code is required")
    private String code;

    private String description;
    private PromotionType type; // Changé de String à PromotionType
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount; // Changé de minPurchaseAmount
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit; // Changé de maxUsage
    private Integer usageLimitPerUser; // Changé de maxUsagePerUser
    private LocalDateTime validFrom; // Changé de startDate
    private LocalDateTime validUntil; // Changé de endDate
    private Boolean isActive;
    private Boolean isFirstOrderOnly;
    private Long applicableCategoryId; // Ajouté
}
