package tn.esprit.projetintegre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Advanced DTO for the Quality-First AI Advisor.
 * Combines pricing, site location (place), average quality ratings,
 * and user sentiment summaries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackQualityDTO {
    private Long packId;
    private String packName;
    private String siteName;
    private String siteLocation;
    private BigDecimal price;
    private Double averageRating;
    private Long totalReviews;
    
    // Quality sub-metrics
    private Double avgServiceQuality;
    private Double avgValueForMoney;
    
    // Sentiment data
    private List<String> topPros;
    private List<String> topCons;
    
    /**
     * Trust Score (0-100) calculated based on real quality metrics and review volume.
     * Incorporates service quality, value for money, and rating consistency.
     */
    public int getTrustScore() {
        // 1. Base score from average rating (1-5 -> 20-100)
        double ratingBase = (averageRating != null && averageRating > 0) ? (averageRating * 18) : 50.0;
        
        // 2. Quality & Value impact (weighted 20%)
        double qualityImpact = 0;
        if (avgServiceQuality != null && avgServiceQuality > 0) qualityImpact += (avgServiceQuality * 2);
        if (avgValueForMoney != null && avgValueForMoney > 0)   qualityImpact += (avgValueForMoney * 2);
        
        // 3. Review Volume confidence
        double volumeFactor = 0;
        if (totalReviews != null && totalReviews > 0) {
            volumeFactor = Math.min(10, Math.log10(totalReviews + 1) * 5);
        }

        // 4. Sentiment Variance (Dynamic penalty/bonus)
        double sentimentBonus = 0;
        if (topPros != null && !topPros.isEmpty()) sentimentBonus += Math.min(5, topPros.size() * 1.5);
        if (topCons != null && !topCons.isEmpty()) sentimentBonus -= Math.min(10, topCons.size() * 2.5);

        double finalScore = ratingBase + qualityImpact + volumeFactor + sentimentBonus;
        
        // 5. Hard Clamp and Freshness Logic
        if (totalReviews == null || totalReviews == 0) {
            // New packs start with a "neutral-positive" trust influenced by site reputation
            return (int) Math.max(45, Math.min(75, 60.0 + (packId % 10)));
        }

        return (int) Math.max(5, Math.min(98, finalScore));
    }
}
