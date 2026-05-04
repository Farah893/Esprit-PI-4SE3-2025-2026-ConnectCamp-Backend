package tn.esprit.projetintegre.dto.ml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ML response for the CampingService module.
 * Fields are populated depending on which prediction endpoint was called.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceMLResponse {

    // ── Rating prediction ─────────────────────────────────────────────────
    @JsonProperty("predictedRating")
    private Double predictedRating;

    /** Human label: "Excellent", "Très bien", "Bien", "Moyen", "À améliorer" */
    @JsonProperty("ratingLabel")
    private String ratingLabel;

    // ── Demand prediction ─────────────────────────────────────────────────
    @JsonProperty("predictedDemand")
    private String predictedDemand;   // LOW / MEDIUM / HIGH

    @JsonProperty("demandProbabilities")
    private Map<String, Double> demandProbabilities;

    @JsonProperty("confidence")
    private Double confidence;

    /** Actionable advice text returned by the ML server */
    @JsonProperty("advice")
    private String advice;

    // ── Shared ────────────────────────────────────────────────────────────
    @JsonProperty("inputSummary")
    private Map<String, Object> inputSummary;

    /** Whether the ML server returned an error */
    private boolean error;
    private String  errorMessage;
}
