package tn.esprit.projetintegre.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.response.RecommendationAIResponse;
import tn.esprit.projetintegre.services.RecommendationService;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<RecommendationAIResponse> recommend(@PathVariable Long eventId) {
        RecommendationAIResponse response = recommendationService.recommend(eventId);
        if (response.getError() != null) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
