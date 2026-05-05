package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.PageResponse;

import tn.esprit.projetintegre.dto.request.ProductRequest;
import tn.esprit.projetintegre.dto.request.PriceCalculationRequest;

import tn.esprit.projetintegre.dto.response.ProductResponse;
import tn.esprit.projetintegre.dto.response.ProductQualityScoreResponse;
import tn.esprit.projetintegre.dto.response.PriceCalculationResponse;

import tn.esprit.projetintegre.entities.Product;

import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.GeoLocationService;
import tn.esprit.projetintegre.services.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = {"http://localhost:4200", "https://lively-wave-019e62b03.7.azurestaticapps.net"}, allowCredentials = "true")
public class ProductController {

    private final ProductService productService;
    private final DtoMapper dtoMapper;
    private final GeoLocationService geoLocationService;

    @GetMapping
    @Operation(summary = "Get all active products with pagination")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<Product> products = productService.getActiveProducts(PageRequest.of(page, size, sort));
        Page<ProductResponse> response = products.map(dtoMapper::toProductResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        productService.incrementViewCount(id);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toProductResponse(product)));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> products = productService.getProductsByCategory(categoryId, PageRequest.of(page, size));
        Page<ProductResponse> response = products.map(dtoMapper::toProductResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get products by seller")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsBySeller(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> products = productService.getProductsBySeller(sellerId, PageRequest.of(page, size));
        Page<ProductResponse> response = products.map(dtoMapper::toProductResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> products = productService.searchProducts(keyword, PageRequest.of(page, size));
        Page<ProductResponse> response = products.map(dtoMapper::toProductResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/{id}/quality-score")
    @Operation(summary = "Get product quality score",
            description = "Returns a 0-100 quality score with breakdown across completeness, media, reviews, performance and seller dimensions.")
    public ResponseEntity<ApiResponse<ProductQualityScoreResponse>> getProductQualityScore(
            @PathVariable Long id) {

        ProductQualityScoreResponse response = productService.calculateQualityScore(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    @PostMapping("/quality-score/batch")
    @Operation(summary = "Get quality scores for multiple products",
            description = "Accepts a JSON array of product IDs and returns quality scores for each.")
    public ResponseEntity<ApiResponse<List<ProductQualityScoreResponse>>> getProductQualityScoresBatch(
            @RequestBody List<Long> productIds) {

        List<ProductQualityScoreResponse> responses = productService.calculateQualityScoresBatch(productIds);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    @GetMapping("/quality-ranking")
    @Operation(summary = "Get products ranked by quality score",
            description = "Returns a paginated list of products sorted by their quality score in descending order.")
    public ResponseEntity<ApiResponse<PageResponse<ProductQualityScoreResponse>>> getTopQualityProducts(
            @RequestParam(defaultValue = "0")            int    page,
            @RequestParam(defaultValue = "10")           int    size,
            @RequestParam(defaultValue = "overallScore") String sortBy,
            @RequestParam(defaultValue = "desc")         String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Page<ProductQualityScoreResponse> resultPage =
                productService.getTopQualityProducts(PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(resultPage)));
    }

    @GetMapping("/{id}/price")
    public ResponseEntity<ApiResponse<PriceCalculationResponse>> calculateProductPrice(
            @PathVariable Long id,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String postalCode,
            @RequestParam(required = false, defaultValue = "1") Integer quantity,
            HttpServletRequest httpRequest) {

        // 🔥 1. récupérer IP
        String ip = geoLocationService.getClientIp(httpRequest);

        // 🔥 2. auto localisation SI country non fourni
        if (country == null || country.isBlank()) {
            country = geoLocationService.getCountryFromIp(ip);
        }

        // 🔥 3. construire request
        PriceCalculationRequest request = PriceCalculationRequest.builder()
                .productId(id)
                .variantId(variantId)
                .countryCode(country)
                .postalCode(postalCode)
                .quantity(quantity)
                .build();

        PriceCalculationResponse response = productService.calculatePrice(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
    @PostMapping("/price/calculate-batch")
    @Operation(
            summary = "Batch price calculation for cart",
            description = "Calculates localised prices for multiple products in a single request (e.g. cart checkout)."
    )
    public ResponseEntity<ApiResponse<List<PriceCalculationResponse>>> calculatePricesBatch(
            @RequestBody List<PriceCalculationRequest> requests) {

        List<PriceCalculationResponse> responses = productService.calculatePricesBatch(requests);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> products = productService.getProductsByPriceRange(min, max, PageRequest.of(page, size));
        Page<ProductResponse> response = products.map(dtoMapper::toProductResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeaturedProducts() {
        List<Product> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toProductResponseList(products)));
    }

    @GetMapping("/top-selling")
    @Operation(summary = "Get top selling products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<Product> products = productService.getTopSellingProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toProductResponseList(products)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Créer un nouveau produit")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        Product created = productService.createProduct(mapToProduct(request), request.getCategoryId(), request.getSellerId());
        return ResponseEntity.ok(ApiResponse.success("Produit créé avec succès", dtoMapper.toProductResponse(created)));
    }
    @GetMapping("/price/available-countries")
    @Operation(
            summary = "List supported countries with tax info",
            description = "Returns all countries the platform can ship to, including VAT rate and shipping zone details."
    )
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableCountries() {
        List<Map<String, Object>> countries = productService.getAvailableCountries();
        return ResponseEntity.ok(ApiResponse.success(countries));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Mettre à jour un produit")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        Product updated = productService.updateProduct(id, mapToProduct(request));
        return ResponseEntity.ok(ApiResponse.success("Produit mis à jour avec succès", dtoMapper.toProductResponse(updated)));
    }
    
    private Product mapToProduct(ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .discountPercentage(request.getDiscountPercentage())
                .sku(request.getSku())
                .barcode(request.getBarcode())
                .brand(request.getBrand())
                .stockQuantity(request.getStockQuantity())
                .minStockLevel(request.getMinStockLevel())
                .maxStockLevel(request.getMaxStockLevel())
                .trackInventory(request.getTrackInventory())
                .images(request.getImages())
                .thumbnail(request.getThumbnail())
                .isFeatured(request.getIsFeatured())
                .isOnSale(request.getIsOnSale())
                .isRentable(request.getIsRentable())
                .rentalPricePerDay(request.getRentalPricePerDay())
                .weight(request.getWeight())
                .dimensions(request.getDimensions())
                .tags(request.getTags())
                .build();
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Update product stock")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        Product updated = productService.updateStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", dtoMapper.toProductResponse(updated)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Delete a product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
    }
}
