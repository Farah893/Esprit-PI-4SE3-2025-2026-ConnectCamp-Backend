package tn.esprit.projetintegre.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Result DTO for Risk Score API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteRiskScoreDTO {

    private Long   siteId;
    private String siteName;

    /** Normalized risk score between 0 and 100. */
    private Integer riskScore;

    /** Textual level: SAFE | WATCH | DANGER | CRITICAL */
    private String riskLevel;

    /** Total active alerts on site. */
    private Integer activeAlertCount;

    /** Number of active CRITICAL alerts. */
    private Integer criticalAlertCount;

    /** Number of unacknowledged (ACTIVE) alerts. */
    private Integer unacknowledgedCount;

    /** Timestamp of the last reported alert on this site. */
    private LocalDateTime lastAlertAt;
}
