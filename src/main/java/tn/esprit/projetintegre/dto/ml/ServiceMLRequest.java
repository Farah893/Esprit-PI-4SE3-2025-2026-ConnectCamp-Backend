package tn.esprit.projetintegre.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request sent to the Python ML server for CampingService predictions:
 *  POST /api/ml/services/predict          — rating + demand (combined)
 *  POST /api/ml/services/predict-rating   — rating only
 *  POST /api/ml/services/predict-demand   — demand only
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceMLRequest {

    /** Service type: ACCOMMODATION, CATERING, TRANSPORT, SECURITY,
     *  MEDICAL, ENTERTAINMENT, GUIDE, OTHER */
    @JsonProperty("serviceType")
    private String serviceType;

    /** Price in euros */
    @JsonProperty("priceEur")
    @Builder.Default
    private double priceEur = 50.0;

    /** Service duration in minutes */
    @JsonProperty("durationMinutes")
    @Builder.Default
    private int durationMinutes = 120;

    /** Maximum number of persons / capacity */
    @JsonProperty("maxCapacity")
    @Builder.Default
    private int maxCapacity = 10;

    /** Season: 1=spring, 2=summer, 3=autumn, 4=winter */
    @JsonProperty("season")
    @Builder.Default
    private int season = 2;

    /** True if reserved exclusively for campers */
    @JsonProperty("isCamperOnly")
    @Builder.Default
    private boolean isCamperOnly = false;

    /** True if the service is offered by an organizer */
    @JsonProperty("isOrganizerService")
    @Builder.Default
    private boolean isOrganizerService = false;

    /** Quality score of the site/location (1–10) */
    @JsonProperty("locationScore")
    @Builder.Default
    private int locationScore = 5;

    /** Provider's years of experience */
    @JsonProperty("providerExperienceYears")
    @Builder.Default
    private int providerExperienceYears = 3;

    /** Number of existing reviews */
    @JsonProperty("reviewCount")
    @Builder.Default
    private int reviewCount = 0;

    /** Current / known rating — used only for demand prediction endpoint */
    @JsonProperty("rating")
    private Double rating;
}
