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

import tn.esprit.projetintegre.dto.ReviewResponseDTO;
import java.util.List;
import java.util.stream.Collectors;
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
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getSiteReviews(
            @PathVariable Long siteId,
            Pageable pageable) {
        Page<Review> page = reviewService.getReviewsByTarget(ReviewTargetType.SITE, siteId, pageable);
        List<ReviewResponseDTO> dtos = page.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PostMapping("/site/{siteId}")
    @Operation(summary = "Create a review for a site (Alias)")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> createSiteReview(
            @PathVariable Long siteId,
            @RequestBody Review review,
            @RequestParam(required = false) Long userId) {
        
        Long finalUserId = userId;
        // 1. Try to get userId from Security Context (Most reliable)
        if (finalUserId == null) {
            String email = SecurityUtil.getCurrentUserEmail();
            System.out.println("DEBUG: Review creation - Security context email: " + email);
            if (email != null) {
                finalUserId = userRepository.findByUsername(email)
                        .or(() -> userRepository.findByEmail(email))
                        .map(tn.esprit.projetintegre.entities.User::getId)
                        .orElse(null);
                System.out.println("DEBUG: Resolved finalUserId from email: " + finalUserId);
            }
        }
        
        // 2. Try to get userId from the review object body
        if (finalUserId == null && review.getUser() != null) {
            finalUserId = review.getUser().getId();
            System.out.println("DEBUG: Resolved finalUserId from review body: " + finalUserId);
        }
        
        if (finalUserId == null) {
            System.err.println("ERROR: No userId found for review creation!");
            return ResponseEntity.badRequest().body(ApiResponse.error("User ID is required. Please make sure you are logged in."));
        }

        review.setTargetType(ReviewTargetType.SITE);
        review.setTargetId(siteId);
        Review saved = reviewService.createReview(review, finalUserId);
        return ResponseEntity.ok(ApiResponse.success("Review submitted successfully", mapToDTO(saved)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a review (Alias)")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> updateReview(
            @PathVariable Long id,
            @RequestBody Review reviewDetails) {
        Review updated = reviewService.updateReview(id, reviewDetails);
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", mapToDTO(updated)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review (Alias)")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }

    private ReviewResponseDTO mapToDTO(Review review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .targetType(review.getTargetType())
                .targetId(review.getTargetId())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .images(review.getImages())
                .likesCount(review.getLikesCount())
                .verified(review.getVerified())
                .approved(review.getApproved())
                .response(review.getResponse())
                .respondedAt(review.getRespondedAt())
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .userName(review.getUser() != null ? review.getUser().getName() : "Anonymous")
                .userAvatar(review.getUser() != null ? review.getUser().getAvatar() : null)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
