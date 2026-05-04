package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.*;
import tn.esprit.projetintegre.services.AiService;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@Tag(name = "AI Analytics", description = "Endpoints pour les analyses IA et Groq LLaMA")
public class AiController {

    private final AiService aiService;

    @PostMapping("/sitrep")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate an AI SITREP of active alerts (ADMIN)")
    public ResponseEntity<ApiResponse<AiSitrepResponseDTO>> generateSitrep() {
        AiSitrepResponseDTO result = aiService.generateSitrep();
        return ResponseEntity.ok(ApiResponse.success("SITREP generated successfully", result));
    }

    @PostMapping("/pack-advisor")
    @Operation(summary = "Natural language AI Pack Advisor")
    public ResponseEntity<ApiResponse<AiPackAdvisorResponseDTO>> advisePacks(
            @RequestBody AiPackAdvisorRequestDTO request) {
        AiPackAdvisorResponseDTO result = aiService.advisePacks(request);
        return ResponseEntity.ok(ApiResponse.success("AI recommendation generated", result));
    }

    @PostMapping("/service-reputation/{serviceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER', 'SELLER', 'CAMPER', 'PARTICIPANT', 'USER')")
    @Operation(summary = "AI sentiment analysis for a service")
    public ResponseEntity<ApiResponse<String>> analyzeReputation(@PathVariable Long serviceId) {
        String result = aiService.analyzeServiceReputationById(serviceId);
        return ResponseEntity.ok(ApiResponse.success("Reputation analysis generated", result));
    }

    @PostMapping("/pack-reputation/{packId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER', 'CAMPER', 'PARTICIPANT', 'USER')")
    @Operation(summary = "AI sentiment analysis for a pack bundle")
    public ResponseEntity<ApiResponse<String>> analyzePackReputation(@PathVariable Long packId) {
        String result = aiService.analyzePackReputationById(packId);
        return ResponseEntity.ok(ApiResponse.success("Pack reputation analysis generated", result));
    }

    @PostMapping("/generate-pack-description")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate a marketing description for a pack (Magic Draft)")
    public ResponseEntity<ApiResponse<String>> generateDescription(
            @RequestParam String name,
            @RequestParam String services,
            @RequestParam String site) {
        String result = aiService.generatePackDescription(name, services, site);
        return ResponseEntity.ok(ApiResponse.success("Description generated", result));
    }

    @PostMapping("/detect-fraud")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    @Operation(summary = "AI Candidate fraud detection (Scan Bio)")
    public ResponseEntity<ApiResponse<Boolean>> detectFraud(
            @RequestParam String name,
            @RequestParam String bio) {
        boolean isFraud = aiService.detectCandidatureFraud(name, bio);
        return ResponseEntity.ok(ApiResponse.success("Fraud detection complete", isFraud));
    }

    @PostMapping("/evaluate-candidate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    @Operation(summary = "AI Candidate compatibility analysis (Detailed Scan)")
    public ResponseEntity<ApiResponse<AiCandidatureResponseDTO>> evaluateCandidate(
            @RequestParam String name,
            @RequestParam String bio,
            @RequestParam String serviceName,
            @RequestParam String serviceDesc,
            @RequestParam String eventTitle,
            @RequestParam String eventDesc) {
        AiCandidatureResponseDTO result = aiService.evaluateCandidateMatching(
                name, bio, serviceName, serviceDesc, eventTitle, eventDesc);
        return ResponseEntity.ok(ApiResponse.success("Candidate evaluation complete", result));
    }
}
