package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import tn.esprit.projetintegre.dto.AlertWithInterventionStatsDTO;
import tn.esprit.projetintegre.dto.InterventionEfficiencyDTO;
import tn.esprit.projetintegre.dto.SiteRiskScoreDTO;
import tn.esprit.projetintegre.exception.AccessDeniedException;
import tn.esprit.projetintegre.security.SecurityUtil;
import tn.esprit.projetintegre.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.dto.EmergencyAlertDTO;
import tn.esprit.projetintegre.entities.EmergencyAlert;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.entities.Site;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.enums.AlertStatus;
import tn.esprit.projetintegre.enums.EmergencySeverity;
import tn.esprit.projetintegre.enums.EmergencyType;
import tn.esprit.projetintegre.exception.BusinessException;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.EmergencyAlertRepository;
import tn.esprit.projetintegre.repositories.UserRepository;
import tn.esprit.projetintegre.repositories.SiteRepository;
import tn.esprit.projetintegre.repositories.EventRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmergencyAlertService {

    private final EmergencyAlertRepository alertRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    public EmergencyAlertDTO.Response createAlert(Long reporterId, EmergencyAlertDTO.CreateRequest request) {
        if (!SecurityUtil.hasRole(Role.CAMPER) && !SecurityUtil.hasRole(Role.PARTICIPANT) && !SecurityUtil.hasRole(Role.USER)) {
            throw new AccessDeniedException("Only campers and users can initiate SOS alerts");
        }
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + reporterId));

        EmergencyAlert alert = EmergencyAlert.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .emergencyType(request.getEmergencyType())
                .severity(request.getSeverity())
                .status(AlertStatus.ACTIVE)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .location(request.getLocation())
                .instructions(request.getInstructions())
                .emergencyContacts(request.getEmergencyContacts())
                .affectedPersonsCount(request.getAffectedPersonsCount())
                .evacuationRequired(request.getEvacuationRequired() != null ? request.getEvacuationRequired() : false)
                .reportedBy(reporter)
                .build();

        if (request.getSiteId() != null) {
            Site site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Site non trouvé"));
            alert.setSite(site);
        }

        if (request.getEventId() != null) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));
            alert.setEvent(event);
        }

        alert = alertRepository.save(alert);
        return toResponse(alert);
    }

    @Transactional(readOnly = true)
    public List<EmergencyAlertDTO.Response> getActiveAlerts() {
        return alertRepository.findActiveAlerts().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EmergencyAlertDTO.Response> getAllAlerts() {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can view all alerts history");
        }
        return alertRepository.findAllWithDetails().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertWithInterventionStatsDTO> getActiveAlertsWithInterventionStats() {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can access alert statistics");
        }
        return alertRepository.findActiveAlertsWithInterventionStats();
    }

    @Transactional(readOnly = true)
    public SiteRiskScoreDTO calculateSiteRiskScore(Long siteId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site non trouvé avec l'ID: " + siteId));

        List<EmergencyAlert> activeAlerts = alertRepository.findActiveAlertsBySite(siteId);
        LocalDateTime now = LocalDateTime.now();

        double rawScore = 0.0;
        int criticalCount = 0;
        int unacknowledgedCount = 0;
        LocalDateTime lastAlertAt = null;

        for (EmergencyAlert alert : activeAlerts) {
            int severityWeight = switch (alert.getSeverity()) {
                case LOW      -> 1;
                case MEDIUM   -> 3;
                case HIGH     -> 7;
                case CRITICAL -> 15;
            };

            long ageHours = ChronoUnit.HOURS.between(alert.getReportedAt(), now);
            double timeFactor = ageHours < 2 ? 1.0 : ageHours < 4 ? 1.5 : ageHours < 8 ? 2.0 : 3.0;

            rawScore += severityWeight * timeFactor;

            if (alert.getSeverity() == EmergencySeverity.CRITICAL) criticalCount++;
            if (alert.getStatus() == AlertStatus.ACTIVE) unacknowledgedCount++;

            if (lastAlertAt == null || alert.getReportedAt().isAfter(lastAlertAt)) {
                lastAlertAt = alert.getReportedAt();
            }
        }

        int score = (int) Math.min(100, rawScore * 2.5);
        String riskLevel = score <= 20 ? "SAFE" : score <= 50 ? "WATCH" : score <= 80 ? "DANGER" : "CRITICAL";

        return new SiteRiskScoreDTO(siteId, site.getName(), score, riskLevel,
                activeAlerts.size(), criticalCount, unacknowledgedCount, lastAlertAt);
    }

    @Transactional(readOnly = true)
    public List<InterventionEfficiencyDTO> getInterventionEfficiency() {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can access efficiency data");
        }
        List<InterventionEfficiencyDTO> efficiencies = alertRepository.findInterventionEfficiencyByType();
        Map<EmergencyType, Long> resolvedCounts = alertRepository.countResolvedByType()
                .stream().collect(Collectors.toMap(row -> (EmergencyType) row[0], row -> (Long) row[1]));

        for (InterventionEfficiencyDTO dto : efficiencies) {
            long resolved = resolvedCounts.getOrDefault(dto.getEmergencyType(), 0L);
            dto.setResolutionRate(dto.getTotalAlerts() > 0 ? Math.round((resolved * 1000.0 / dto.getTotalAlerts())) / 10.0 : 0.0);
            dto.setAvgInterventionsPerAlert(dto.getTotalAlerts() > 0 ? Math.round((dto.getTotalInterventions() * 10.0 / dto.getTotalAlerts())) / 10.0 : 0.0);
        }
        return efficiencies;
    }

    public EmergencyAlertDTO.Response acknowledgeAlert(Long id, Long userId) {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can acknowledge alerts");
        }
        EmergencyAlert alert = alertRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alerte non trouvée"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(user);
        return toResponse(alertRepository.save(alert));
    }

    public EmergencyAlertDTO.Response resolveAlert(Long id, Long userId, String notes) {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can resolve alerts");
        }
        EmergencyAlert alert = alertRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alerte non trouvée"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(user);
        alert.setResolutionNotes(notes);
        return toResponse(alertRepository.save(alert));
    }

    public EmergencyAlert getAlertEntity(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency alert not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public EmergencyAlertDTO.Response getById(Long id) {
        EmergencyAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency alert not found with id: " + id));
        return toResponse(alert);
    }

    @Transactional(readOnly = true)
    public List<EmergencyAlertDTO.Response> getAlertsByReporter(Long reporterId) {
        return alertRepository.findByReportedByIdOrderByReportedAtDesc(reporterId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private EmergencyAlertDTO.Response toResponse(EmergencyAlert alert) {
        return EmergencyAlertDTO.Response.builder()
                .id(alert.getId())
                .alertCode(alert.getAlertCode())
                .title(alert.getTitle())
                .description(alert.getDescription())
                .emergencyType(alert.getEmergencyType())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .location(alert.getLocation())
                .instructions(alert.getInstructions())
                .emergencyContacts(alert.getEmergencyContacts())
                .affectedPersonsCount(alert.getAffectedPersonsCount())
                .evacuationRequired(alert.getEvacuationRequired())
                .notificationsSent(alert.getNotificationsSent())
                .reportedAt(alert.getReportedAt())
                .siteId(alert.getSite() != null ? alert.getSite().getId() : null)
                .siteName(alert.getSite() != null ? alert.getSite().getName() : null)
                .reportedById(alert.getReportedBy().getId())
                .reportedByName(alert.getReportedBy().getName())
                .build();
    }
}
