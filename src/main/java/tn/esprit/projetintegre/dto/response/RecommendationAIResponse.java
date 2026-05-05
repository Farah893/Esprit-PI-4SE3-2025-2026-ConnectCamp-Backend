package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationAIResponse {

    private Long eventId;
    private CategoryResult bestCategory;
    private List<ProductSuggestion> products;
    private String error; // null si succès

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryResult {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductSuggestion {
        private Long productId;
        private String productName;     // ← vrai nom depuis BD
        private BigDecimal price;
        private double score;
        private String reason;
    }
}
