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
            @RequestParam Long userId) {
        review.setTargetType(ReviewTargetType.SITE);
        review.setTargetId(siteId);
        return ResponseEntity.ok(ApiResponse.success("Review created successfully",
                reviewService.createReview(review, userId)));
    }
}
