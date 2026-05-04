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
@RequestMapping("/api/general-reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@Tag(name = "General Reviews", description = "Polymorphic reviews for Events, Products, etc.")
public class GeneralReviewController {

    private final GeneralReviewService reviewService;

    @GetMapping("/{targetType}/{targetId}")
    @Operation(summary = "Get reviews for a target")
    public ResponseEntity<ApiResponse<PageResponse<Review>>> getReviews(
            @PathVariable ReviewTargetType targetType,
            @PathVariable Long targetId,
            Pageable pageable) {
        Page<Review> page = reviewService.getReviewsByTarget(targetType, targetId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @PostMapping
    @Operation(summary = "Create a new review")
    public ResponseEntity<ApiResponse<Review>> createReview(
            @RequestBody Review review,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Review created successfully",
                reviewService.createReview(review, userId)));
    }

    @GetMapping("/{targetType}/{targetId}/stats")
    @Operation(summary = "Get rating stats for a target")
    public ResponseEntity<ApiResponse<RatingStats>> getStats(
            @PathVariable ReviewTargetType targetType,
            @PathVariable Long targetId) {
        Double average = reviewService.getAverageRating(targetType, targetId);
        Long count = reviewService.countReviews(targetType, targetId);
        return ResponseEntity.ok(ApiResponse.success(new RatingStats(average, count)));
    }

    private record RatingStats(Double averageRating, Long reviewCount) {
    }
}
