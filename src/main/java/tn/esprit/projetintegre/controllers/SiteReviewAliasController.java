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
import tn.esprit.projetintegre.entities.Review;
import tn.esprit.projetintegre.enums.ReviewTargetType;
import tn.esprit.projetintegre.services.GeneralReviewService;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Site Reviews Alias", description = "Alias for site reviews to match frontend expectations")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class SiteReviewAliasController {

    private final GeneralReviewService reviewService;

    @GetMapping("/site/{siteId}")
    @Operation(summary = "Get reviews for a site (Alias)")
    public ResponseEntity<ApiResponse<PageResponse<Review>>> getSiteReviews(
            @PathVariable Long siteId,
            Pageable pageable) {
        Page<Review> page = reviewService.getReviewsByTarget(ReviewTargetType.SITE, siteId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @PostMapping("/site/{siteId}")
    @Operation(summary = "Create a review for a site (Alias)")
    public ResponseEntity<ApiResponse<Review>> createSiteReview(
            @PathVariable Long siteId,
            @RequestBody Review review,
            @RequestParam(required = false) Long userId) {
        
        Long finalUserId = userId;
        // 1. Try to get userId from Security Context (Most reliable)
        if (finalUserId == null) {
            try {
                finalUserId = tn.esprit.projetintegre.security.SecurityUtil.getCurrentUserId();
            } catch (Exception e) {
                // Ignore and try next method
            }
        }
        
        // 2. Try to get userId from the review object body
        if (finalUserId == null && review.getUser() != null) {
            finalUserId = review.getUser().getId();
        }
        
        if (finalUserId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User ID is required. Please make sure you are logged in."));
        }

        review.setTargetType(ReviewTargetType.SITE);
        review.setTargetId(siteId);
        return ResponseEntity.ok(ApiResponse.success("Review submitted successfully",
                reviewService.createReview(review, finalUserId)));
    }
}
