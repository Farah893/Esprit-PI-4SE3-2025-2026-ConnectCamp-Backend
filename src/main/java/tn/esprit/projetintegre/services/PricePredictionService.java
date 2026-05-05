package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tn.esprit.projetintegre.entities.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricePredictionService {

    @Value("${ml.base-url:http://localhost:8000}")
    private String mlBaseUrl;

    public PricePredictionResult predict(Product product) {
        try {
            WebClient client = WebClient.create(mlBaseUrl);

            Map<String, Object> body = Map.ofEntries(
                    Map.entry("name",              product.getName()),
                    Map.entry("brand",             product.getBrand() != null ? product.getBrand() : "Unknown"),
                    Map.entry("categoryName",      product.getCategory() != null ? product.getCategory().getName() : "Outdoor Accessories"),
                    Map.entry("weight",            product.getWeight() != null ? product.getWeight() : 0.0),
                    Map.entry("stockQuantity",     product.getStockQuantity() != null ? product.getStockQuantity() : 0),
                    Map.entry("minStockLevel",     product.getMinStockLevel() != null ? product.getMinStockLevel() : 0),
                    Map.entry("rating",            product.getRating() != null ? product.getRating() : 3.0),
                    Map.entry("reviewCount",       product.getReviewCount() != null ? product.getReviewCount() : 0),
                    Map.entry("salesCount",        product.getSalesCount() != null ? product.getSalesCount() : 0),
                    Map.entry("viewCount",         product.getViewCount() != null ? product.getViewCount() : 0),
                    Map.entry("isFeatured",        product.getIsFeatured() != null && product.getIsFeatured()),
                    Map.entry("isOnSale",          false),
                    Map.entry("isRentable",        product.getIsRentable() != null && product.getIsRentable()),
                    Map.entry("rentalPricePerDay", product.getRentalPricePerDay() != null ? product.getRentalPricePerDay() : 0.0),
                    Map.entry("tags",              product.getTags() != null ? product.getTags() : List.of()),
                    Map.entry("imagesCount",       product.getImages() != null ? product.getImages().size() : 1),
                    Map.entry("competitorPrice",   0.0),
                    Map.entry("supplierCost",      0.0),
                    Map.entry("shippingCost",      0.0)
            );

            Map response = client.post()
                    .uri("/predict-price")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) throw new RuntimeException("Empty response from ML service");

            double predicted = ((Number) response.get("predictedPrice")).doubleValue();
            String confidence = (String) response.get("confidence");
            Map<String, Object> range = (Map<String, Object>) response.get("priceRange");
            double min = ((Number) range.get("min")).doubleValue();
            double max = ((Number) range.get("max")).doubleValue();

            return new PricePredictionResult(
                    BigDecimal.valueOf(predicted).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(min).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(max).setScale(2, RoundingMode.HALF_UP),
                    confidence
            );

        } catch (Exception e) {
            log.warn("Price prediction failed for product {}: {}", product.getId(), e.getMessage());
            return null;
        }
    }

    public record PricePredictionResult(
            BigDecimal predictedPrice,
            BigDecimal priceMin,
            BigDecimal priceMax,
            String confidence
    ) {}
}
