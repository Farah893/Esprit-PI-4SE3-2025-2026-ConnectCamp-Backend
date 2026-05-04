package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.request.WishlistRequest;
import tn.esprit.projetintegre.dto.response.WishlistResponse;
import tn.esprit.projetintegre.entities.Wishlist;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.WishlistService;

import java.util.List;

@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
@Tag(name = "Wishlists", description = "Wishlist management APIs")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class WishlistController {

    private final WishlistService wishlistService;
    private final DtoMapper dtoMapper;

    @GetMapping
    @Operation(summary = "Get all wishlists")
    public ResponseEntity<ApiResponse<List<WishlistResponse>>> getAllWishlists() {
        List<Wishlist> wishlists = wishlistService.getAllWishlists();
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toWishlistResponseList(wishlists)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wishlist by ID")
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlistById(@PathVariable Long id) {
        Wishlist wishlist = wishlistService.getWishlistById(id);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toWishlistResponse(wishlist)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get wishlists by user ID")
    public ResponseEntity<ApiResponse<List<WishlistResponse>>> getWishlistsByUserId(@PathVariable Long userId) {
        List<Wishlist> wishlists = wishlistService.getWishlistsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toWishlistResponseList(wishlists)));
    }

    @PostMapping
    @Operation(summary = "Create a new wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> createWishlist(
            @Valid @RequestBody WishlistRequest request,
            @RequestParam Long userId) {
        Wishlist wishlist = toEntity(request);
        Wishlist created = wishlistService.createWishlist(wishlist, userId);
        return ResponseEntity.ok(ApiResponse.success("Wishlist created successfully", dtoMapper.toWishlistResponse(created)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> updateWishlist(
            @PathVariable Long id,
            @Valid @RequestBody WishlistRequest request) {
        Wishlist wishlistDetails = toEntity(request);
        Wishlist updated = wishlistService.updateWishlist(id, wishlistDetails);
        return ResponseEntity.ok(ApiResponse.success("Wishlist updated successfully", dtoMapper.toWishlistResponse(updated)));
    }

    @PostMapping("/{wishlistId}/products/{productId}")
    @Operation(summary = "Add product to wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> addProductToWishlist(
            @PathVariable Long wishlistId,
            @PathVariable Long productId) {
        Wishlist updated = wishlistService.addProductToWishlist(wishlistId, productId);
        return ResponseEntity.ok(ApiResponse.success("Product added to wishlist", dtoMapper.toWishlistResponse(updated)));
    }

    @DeleteMapping("/{wishlistId}/products/{productId}")
    @Operation(summary = "Remove product from wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> removeProductFromWishlist(
            @PathVariable Long wishlistId,
            @PathVariable Long productId) {
        Wishlist updated = wishlistService.removeProductFromWishlist(wishlistId, productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist", dtoMapper.toWishlistResponse(updated)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a wishlist")
    public ResponseEntity<ApiResponse<Void>> deleteWishlist(@PathVariable Long id) {
        wishlistService.deleteWishlist(id);
        return ResponseEntity.ok(ApiResponse.success("Wishlist deleted successfully", null));
    }

    private Wishlist toEntity(WishlistRequest request) {
        return Wishlist.builder()
                .name(request.getName())
                .isPublic(request.getIsPublic())
                .build();
    }
}
