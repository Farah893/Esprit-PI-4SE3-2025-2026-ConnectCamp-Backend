package tn.esprit.projetintegre.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.projetintegre.enums.EmergencyType;

/**
 * Result DTO for Intervention Efficiency API.
 */
@Data
@NoArgsConstructor
public class InterventionEfficiencyDTO {

    private EmergencyType emergencyType;

    /** Total alerts for this type. */
    private Long totalAlerts;

    /** Total interventions launched for this type. */
    private Long totalInterventions;

    /** Average response time in minutes. */
    private Double avgResponseMinutes;

    // --- Calculated fields in service ---

    /** Resolution rate = resolved / total * 100. */
    private Double resolutionRate;

    /** Average interventions per alert. */
    private Double avgInterventionsPerAlert;

    /** Constructor for JPQL new() */
    public InterventionEfficiencyDTO(EmergencyType emergencyType,
                                     Long totalAlerts,
                                     Long totalInterventions,
                                     Double avgResponseMinutes) {
        this.emergencyType      = emergencyType;
        this.totalAlerts        = totalAlerts;
        this.totalInterventions = totalInterventions;
        this.avgResponseMinutes = avgResponseMinutes;
    }
}
