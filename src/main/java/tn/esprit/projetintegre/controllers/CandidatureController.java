package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.entities.CandidatureService;
import tn.esprit.projetintegre.enums.StatutCandidature;
import tn.esprit.projetintegre.services.CandidatureServiceLogic;

@RestController
@RequestMapping("/api/candidatures")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://lively-wave-019e62b03.7.azurestaticapps.net"}, allowCredentials = "true")
@Tag(name = "Candidatures Service", description = "Endpoints for participants to apply as workers and organizers to manage them")
public class CandidatureController {

    private final CandidatureServiceLogic candidatureServiceLogic;

    @PostMapping("/apply/{eventServiceId}")
    @PreAuthorize("hasAnyRole('PARTICIPANT', 'CAMPER')")
    @Operation(summary = "Participant applies for a specific service in an event")
    public ResponseEntity<ApiResponse<Long>> apply(
            @PathVariable Long eventServiceId,
            @RequestParam Long userId,
            @RequestBody CandidatureService candidature) {
        CandidatureService created = candidatureServiceLogic.applyForService(userId, eventServiceId, candidature);
        return ResponseEntity.ok(ApiResponse.success("Candidature soumise avec succès", created.getId()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Organizer updates candidature status (ACCEPTEE, REJETEE, etc.)")
    public ResponseEntity<ApiResponse<CandidatureService>> updateStatus(
            @PathVariable Long id,
            @RequestParam Long organisateurId,
            @RequestParam StatutCandidature status,
            @RequestParam(required = false) String notes) {
        CandidatureService updated = candidatureServiceLogic.updateCandidatureStatus(id, organisateurId, status, notes);
        return ResponseEntity.ok(ApiResponse.success("Statut mis à jour avec succès", updated));
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Participant withdraws their application (only if EN_ATTENTE)")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @PathVariable Long id,
            @RequestParam Long userId) {
        candidatureServiceLogic.withdrawCandidature(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Candidature retirée avec succès", null));
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get all candidatures for a specific event")
    public ResponseEntity<ApiResponse<java.util.List<tn.esprit.projetintegre.entities.CandidatureService>>> getByEvent(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.success(
                candidatureServiceLogic.getCandidaturesByEvent(eventId)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all candidatures for a specific user")
    public ResponseEntity<ApiResponse<java.util.List<tn.esprit.projetintegre.entities.CandidatureService>>> getByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                candidatureServiceLogic.getCandidaturesByUser(userId)));
    }

    @GetMapping("/organizer/{organizerId}")
    @Operation(summary = "Get all candidatures for events owned by a specific organizer")
    public ResponseEntity<ApiResponse<java.util.List<tn.esprit.projetintegre.entities.CandidatureService>>> getByOrganizer(
            @PathVariable Long organizerId) {
        return ResponseEntity.ok(ApiResponse.success(
                candidatureServiceLogic.getCandidaturesByOrganizer(organizerId)));
    }
}
