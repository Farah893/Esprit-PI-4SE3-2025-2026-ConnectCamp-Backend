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
import tn.esprit.projetintegre.repositories.UserRepository;
import tn.esprit.projetintegre.security.SecurityUtil;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Site Reviews Alias", description = "Alias for site reviews to match frontend expectations")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class SiteReviewAliasController {

    private final GeneralReviewService reviewService;
    private final UserRepository userRepository;

    @GetMapping("/site/{siteId}")
    @Operation(summary = "Get reviews for a site (Alias)")
    public ResponseEntity<ApiResponse<List<Review>>> getSiteReviews(
            @PathVariable Long siteId,
            Pageable pageable) {
        Page<Review> page = reviewService.getReviewsByTarget(ReviewTargetType.SITE, siteId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page.getContent()));
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
            String email = SecurityUtil.getCurrentUserEmail();
            if (email != null) {
                finalUserId = userRepository.findByUsername(email)
                        .or(() -> userRepository.findByEmail(email))
                        .map(tn.esprit.projetintegre.entities.User::getId)
                        .orElse(null);
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
