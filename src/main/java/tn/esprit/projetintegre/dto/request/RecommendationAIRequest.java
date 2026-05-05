// package tn.esprit.projetintegre.dto.request;

package tn.esprit.projetintegre.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationAIRequest {

    private Long eventId;
    private String title;
    private String description;
    private String eventType;
    private String category;
    private String location;
    private List<CategoryDTO> categories;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDTO {
        private Long id;
        private String name;
        private String description;
    }
}
