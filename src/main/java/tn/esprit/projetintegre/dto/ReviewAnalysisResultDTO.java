package tn.esprit.projetintegre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAnalysisResultDTO {
    private boolean consistent;
    private String reason;
}
