package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.services.EventInteractionService;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://lively-wave-019e62b03.7.azurestaticapps.net"}, allowCredentials = "true")
@Tag(name = "Event Interactions", description = "Endpoints for likes and dislikes on events")
public class EventInteractionController {

    private final EventInteractionService interactionService;

    @PostMapping("/{id}/like")
    @Operation(summary = "Like an event")
    public ResponseEntity<ApiResponse<Void>> likeEvent(@PathVariable Long id, @RequestParam Long userId) {
        interactionService.handleLike(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Success", null));
    }

    @PostMapping("/{id}/dislike")
    @Operation(summary = "Dislike an event")
    public ResponseEntity<ApiResponse<Void>> dislikeEvent(@PathVariable Long id, @RequestParam Long userId) {
        interactionService.handleDislike(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Success", null));
    }

    @GetMapping("/{id}/my-reaction")
    @Operation(summary = "Get current user reaction (liked/disliked) for an event")
    public ResponseEntity<Map<String, Boolean>> getUserReaction(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(interactionService.getUserReaction(id, userId));
    }
}
