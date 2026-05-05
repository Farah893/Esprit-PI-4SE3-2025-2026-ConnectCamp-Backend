package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.PageResponse;
import tn.esprit.projetintegre.entities.ServiceReview;
import tn.esprit.projetintegre.services.ServiceReviewService;

@RestController
@RequestMapping("/api/service-reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://lively-wave-019e62b03.7.azurestaticapps.net"}, allowCredentials = "true")
@Tag(name = "Service Reviews", description = "Endpoints pour les avis sur les services")
public class ServiceReviewController {

    private final ServiceReviewService serviceReviewService;

    @GetMapping("/service/{serviceId}")
    @Operation(summary = "Get reviews for a specific service")
    public ResponseEntity<ApiResponse<PageResponse<ServiceReview>>> getReviewsByService(
            @PathVariable Long serviceId, Pageable pageable) {
        Page<ServiceReview> reviews = serviceReviewService.getReviewsByService(serviceId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(reviews)));
    }

    @PostMapping("/service/{serviceId}")
    @Operation(summary = "Create or update a review for a service")
    public ResponseEntity<ApiResponse<ServiceReview>> createReview(
            @PathVariable Long serviceId,
            @RequestParam Long userId,
            @RequestBody ServiceReview review) {
        ServiceReview created = serviceReviewService.createReview(review, serviceId, userId);
        if (Boolean.FALSE.equals(created.getIsApproved())) {
            return ResponseEntity.ok(ApiResponse.success("⚠ AI Guard: Inconsistency detected. The review is currently under manual verification.", created));
        }
        return ResponseEntity.ok(ApiResponse.success("✨ Review successfully posted! Thank you for your feedback.", created));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        serviceReviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Manually approve a review")
    public ResponseEntity<ApiResponse<Void>> approveReview(@PathVariable Long id) {
        serviceReviewService.approveReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review approved successfully", null));
    }
}
