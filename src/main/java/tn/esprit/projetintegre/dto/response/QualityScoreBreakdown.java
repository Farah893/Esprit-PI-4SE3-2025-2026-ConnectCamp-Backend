package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityScoreBreakdown {
    private Integer completenessScore;  // 0-25
    private Integer mediaScore;         // 0-15
    private Integer reviewScore;        // 0-25
    private Integer performanceScore;   // 0-20
    private Integer sellerScore;        // 0-15
}
