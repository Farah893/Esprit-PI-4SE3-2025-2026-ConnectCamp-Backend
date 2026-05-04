package tn.esprit.projetintegre.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request sent to the Python ML server to predict:
 *  - severity  (POST /api/ml/emergency/predict-severity)
 *  - response time (POST /api/ml/emergency/predict-response-time)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyMLRequest {

    /** Title of the alert (used for TF-IDF text features) */
    private String title;

    /** Description of the alert (used for TF-IDF text features) */
    private String description;

    /** Emergency type: FIRE, MEDICAL, WEATHER, SECURITY, EVACUATION,
     *  NATURAL_DISASTER, EQUIPMENT_FAILURE, OTHER */
    @JsonProperty("emergencyType")
    private String emergencyType;

    /** Number of people affected */
    @JsonProperty("affectedPersonsCount")
    @Builder.Default
    private int affectedPersonsCount = 1;

    /** Whether evacuation is required */
    @JsonProperty("evacuationRequired")
    @Builder.Default
    private boolean evacuationRequired = false;

    /** Hour of day when the alert was reported (0–23) */
    @JsonProperty("hourOfDay")
    @Builder.Default
    private int hourOfDay = 12;

    /** Day of week (0=Monday … 6=Sunday) */
    @JsonProperty("dayOfWeek")
    @Builder.Default
    private int dayOfWeek = 0;

    /** Known or predicted severity — used only for response-time prediction */
    @JsonProperty("severity")
    private String severity;
}
