package tn.esprit.projetintegre.dto.ml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Unified ML response for the Emergency module.
 * Fields are populated depending on which prediction was requested.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmergencyMLResponse {

    // ── Severity prediction fields ────────────────────────────────────────
    @JsonProperty("predictedSeverity")
    private String predictedSeverity;

    @JsonProperty("confidence")
    private Double confidence;

    /** Map: { "LOW": 0.05, "MEDIUM": 0.15, "HIGH": 0.45, "CRITICAL": 0.35 } */
    @JsonProperty("probabilities")
    private Map<String, Double> probabilities;

    // ── Response-time prediction fields ───────────────────────────────────
    @JsonProperty("predictedMinutes")
    private Double predictedMinutes;

    @JsonProperty("confidenceRange")
    private ConfidenceRange confidenceRange;

    // ── Shared ────────────────────────────────────────────────────────────
    @JsonProperty("inputSummary")
    private Map<String, Object> inputSummary;

    /** Whether the ML server returned an error */
    private boolean error;
    private String  errorMessage;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConfidenceRange {
        private Double min;
        private Double max;
    }
}
