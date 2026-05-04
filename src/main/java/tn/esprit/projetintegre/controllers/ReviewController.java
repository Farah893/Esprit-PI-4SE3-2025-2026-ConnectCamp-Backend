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
import tn.esprit.projetintegre.entities.ProductReview;
import tn.esprit.projetintegre.services.ReviewService;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@Tag(name = "Reviews", description = "Product review management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "Get all reviews paginated")
    public ResponseEntity<ApiResponse<PageResponse<ProductReview>>> getAllReviews(Pageable pageable) {
        Page<ProductReview> page = reviewService.getAllReviews(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID")
    public ResponseEntity<ApiResponse<ProductReview>> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewById(id)));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reviews by product ID")
    public ResponseEntity<ApiResponse<PageResponse<ProductReview>>> getReviewsByProductId(@PathVariable Long productId, Pageable pageable) {
        Page<ProductReview> page = reviewService.getReviewsByProductId(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reviews by user ID")
    public ResponseEntity<ApiResponse<PageResponse<ProductReview>>> getReviewsByUserId(@PathVariable Long userId, Pageable pageable) {
        Page<ProductReview> page = reviewService.getReviewsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @PostMapping
    @Operation(summary = "Create a new review")
    public ResponseEntity<ApiResponse<ProductReview>> createReview(
            @RequestBody ProductReview review,
            @RequestParam Long productId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Review created successfully",
                reviewService.createReview(review, productId, userId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a review")
    public ResponseEntity<ApiResponse<ProductReview>> updateReview(
            @PathVariable Long id,
            @RequestBody ProductReview reviewDetails) {
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully",
                reviewService.updateReview(id, reviewDetails)));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a review")
    public ResponseEntity<ApiResponse<ProductReview>> approveReview(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Review approved successfully",
                reviewService.approveReview(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
}
