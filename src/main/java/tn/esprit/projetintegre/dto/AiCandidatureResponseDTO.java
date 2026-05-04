package tn.esprit.projetintegre.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCandidatureResponseDTO {
    private int score;
    private String summary;
    private List<String> strengths;
    private List<String> risks;
    private String recommendation;
}
