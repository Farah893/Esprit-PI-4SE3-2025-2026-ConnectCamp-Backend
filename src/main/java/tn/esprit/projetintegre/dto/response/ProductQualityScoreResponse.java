package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.projetintegre.enums.QualityBadge;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductQualityScoreResponse {
    private Long productId;
    private String productName;
    private Integer overallScore;       // 0-100
    private QualityBadge badge;
    private String badgeColor;
    private QualityScoreBreakdown breakdown;
    private Boolean isActive;
}