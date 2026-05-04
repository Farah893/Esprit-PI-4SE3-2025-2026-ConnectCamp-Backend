package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.*;
import tn.esprit.projetintegre.dto.ml.EmergencyMLResponse;
import tn.esprit.projetintegre.entities.EmergencyAlert;
import tn.esprit.projetintegre.services.EmergencyAlertService;
import tn.esprit.projetintegre.services.EmergencyMLService;

import java.util.List;

@RestController
@RequestMapping("/api/emergency-alerts")
@RequiredArgsConstructor
@Tag(name = "Emergency Alerts", description = "Emergency alert management APIs")
public class EmergencyAlertController {

    private final EmergencyAlertService alertService;
    private final EmergencyMLService emergencyMLService;

    @PostMapping
    @PreAuthorize("hasRole('CAMPER') or hasRole('PARTICIPANT') or hasRole('USER')")
    @Operation(summary = "Create an SOS alert")
    public ResponseEntity<ApiResponse<EmergencyAlertDTO.Response>> createAlert(
            @RequestParam Long reporterId,
            @Valid @RequestBody EmergencyAlertDTO.CreateRequest request) {
        EmergencyAlertDTO.Response response = alertService.createAlert(reporterId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Emergency alert created successfully", response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active alerts")
    public ResponseEntity<ApiResponse<List<EmergencyAlertDTO.Response>>> getActiveAlerts() {
        System.out.println("DEBUG: getActiveAlerts called");
        List<EmergencyAlertDTO.Response> response = alertService.getActiveAlerts();
        System.out.println("DEBUG: Returning " + response.size() + " active alerts");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all alerts (history) - ADMIN ONLY")
    public ResponseEntity<ApiResponse<List<EmergencyAlertDTO.Response>>> getAllAlerts() {
        List<EmergencyAlertDTO.Response> response = alertService.getAllAlerts();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-alerts")
    @Operation(summary = "Get alerts reported by a specific user")
    public ResponseEntity<ApiResponse<List<EmergencyAlertDTO.Response>>> getMyAlerts(@RequestParam Long reporterId) {
        List<EmergencyAlertDTO.Response> response = alertService.getAlertsByReporter(reporterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une alerte par ID")
    public ResponseEntity<ApiResponse<EmergencyAlertDTO.Response>> getById(@PathVariable Long id) {
        EmergencyAlertDTO.Response response = alertService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Acknowledge an alert")
    public ResponseEntity<ApiResponse<EmergencyAlertDTO.Response>> acknowledgeAlert(
            @PathVariable Long id,
            @RequestParam Long userId) {
        EmergencyAlertDTO.Response response = alertService.acknowledgeAlert(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Alert acknowledged successfully", response));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resolve an alert")
    public ResponseEntity<ApiResponse<EmergencyAlertDTO.Response>> resolveAlert(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(required = false) String resolutionNotes) {
        EmergencyAlertDTO.Response response = alertService.resolveAlert(id, userId, resolutionNotes);
        return ResponseEntity.ok(ApiResponse.success("Alert resolved successfully", response));
    }

    @GetMapping("/risk-score/{siteId}")
    @Operation(summary = "Get real-time risk score for a site")
    public ResponseEntity<ApiResponse<SiteRiskScoreDTO>> getSiteRiskScore(@PathVariable Long siteId) {
        SiteRiskScoreDTO result = alertService.calculateSiteRiskScore(siteId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/intervention-efficiency")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get intervention efficiency metrics (ADMIN)")
    public ResponseEntity<ApiResponse<List<InterventionEfficiencyDTO>>> getInterventionEfficiency() {
        List<InterventionEfficiencyDTO> result = alertService.getInterventionEfficiency();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/stats/with-interventions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get active alerts with intervention stats (ADMIN)")
    public ResponseEntity<ApiResponse<List<AlertWithInterventionStatsDTO>>> getAlertsWithInterventionStats() {
        List<AlertWithInterventionStatsDTO> stats = alertService.getActiveAlertsWithInterventionStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ── ML Prediction endpoints ───────────────────────────────────────────

    @GetMapping("/{id}/ml/predict")
    @Operation(summary = "Predict severity + response time for an existing alert (ML)")
    public ResponseEntity<ApiResponse<EmergencyMLResponse>> predictForAlert(@PathVariable Long id) {
        EmergencyAlert alert = alertService.getAlertEntity(id);
        EmergencyMLResponse result = emergencyMLService.predictFull(alert);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/ml/predict-severity")
    @Operation(summary = "Predict emergency severity from text (ML)")
    public ResponseEntity<ApiResponse<EmergencyMLResponse>> predictSeverity(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String emergencyType,
            @RequestParam(defaultValue = "1") int affectedPersons,
            @RequestParam(defaultValue = "false") boolean evacuationRequired) {
        EmergencyMLResponse result = emergencyMLService.predictSeverity(
                title, description, emergencyType, affectedPersons, evacuationRequired);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
