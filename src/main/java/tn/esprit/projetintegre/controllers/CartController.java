package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.response.CartResponse;
import tn.esprit.projetintegre.entities.Cart;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = {"http://localhost:4200", "https://lively-wave-019e62b03.7.azurestaticapps.net"}, allowCredentials = "true")
public class CartController {

    private final CartService cartService;
    private final DtoMapper dtoMapper;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toCartResponse(cart)));
    }

    @PostMapping("/{userId}/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(
            @PathVariable Long userId,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        Cart cart = cartService.addItemToCart(userId, productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", dtoMapper.toCartResponse(cart)));
    }

    @PutMapping("/{userId}/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        Cart cart = cartService.updateCartItemQuantity(userId, itemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", dtoMapper.toCartResponse(cart)));
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long itemId) {
        Cart cart = cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", dtoMapper.toCartResponse(cart)));
    }

    @PostMapping("/{userId}/promo")
    @Operation(summary = "Apply promo code to cart")
    public ResponseEntity<ApiResponse<CartResponse>> applyPromoCode(
            @PathVariable Long userId,
            @RequestParam String code) {
        Cart cart = cartService.applyPromoCode(userId, code);
        return ResponseEntity.ok(ApiResponse.success("Promo code applied", dtoMapper.toCartResponse(cart)));
    }

    @DeleteMapping("/{userId}/promo")
    @Operation(summary = "Remove promo code from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removePromoCode(@PathVariable Long userId) {
        Cart cart = cartService.removePromoCode(userId);
        return ResponseEntity.ok(ApiResponse.success("Promo code removed", dtoMapper.toCartResponse(cart)));
    }
}
