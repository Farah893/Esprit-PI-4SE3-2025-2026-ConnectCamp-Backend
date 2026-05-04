package tn.esprit.projetintegre.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.projetintegre.enums.AlertStatus;
import tn.esprit.projetintegre.enums.EmergencySeverity;
import tn.esprit.projetintegre.enums.EmergencyType;

import java.time.LocalDateTime;

/**
 * Result DTO for JPQL query with joins:
 * EmergencyAlert -> Site, User (reporter), EmergencyIntervention
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertWithInterventionStatsDTO {

    private Long alertId;
    private String alertCode;
    private String title;
    private EmergencyType emergencyType;
    private EmergencySeverity severity;
    private AlertStatus status;
    private String siteName;
    private String reporterName;
    private Long interventionCount;
    private LocalDateTime reportedAt;
}
