package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.request.CategoryRequest;
import tn.esprit.projetintegre.dto.response.CategoryResponse;
import tn.esprit.projetintegre.dto.response.CategorySalesReportResponse;
import tn.esprit.projetintegre.entities.Category;
import tn.esprit.projetintegre.entities.Order;
import tn.esprit.projetintegre.enums.OrderStatus;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.CategoryService;
import tn.esprit.projetintegre.services.OrderService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = {"http://localhost:4200", "https://lively-wave-019e62b03.7.azurestaticapps.net"}, allowCredentials = "true")
public class CategoryController {

    private final CategoryService categoryService;
    private final DtoMapper dtoMapper;
    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toCategoryResponseList(categories)));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getActiveCategories() {
        List<Category> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toCategoryResponseList(categories)));
    }

    @GetMapping("/root")
    @Operation(summary = "Get root categories (without parent)")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories() {
        List<Category> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toCategoryResponseList(categories)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toCategoryResponse(category)));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(@PathVariable String slug) {
        Category category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toCategoryResponse(category)));
    }

    @GetMapping("/{parentId}/subcategories")
    @Operation(summary = "Get subcategories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getSubcategories(@PathVariable Long parentId) {
        List<Category> subcategories = categoryService.getSubcategories(parentId);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toCategoryResponseList(subcategories)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Create a category (Admin/Seller)")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @RequestParam(required = false) Long parentId) {
        Category category = toEntity(request);
        Category created = categoryService.createCategory(category, parentId);
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", dtoMapper.toCategoryResponse(created)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Update a category (Admin/Seller)")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        Category categoryDetails = toEntity(request);
        Category updated = categoryService.updateCategory(id, categoryDetails);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", dtoMapper.toCategoryResponse(updated)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Delete a category (Admin/Seller)")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }

    // Mapping method
    private Category toEntity(CategoryRequest request) {
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .image(request.getImage())
                .slug(request.getSlug())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
    }
    // Dans OrderController.java — ajouter :

    @GetMapping("/by-category")
    public ResponseEntity<List<Order>> getOrdersByCategory(
            @RequestParam OrderStatus status,
            @RequestParam Long categoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate) {

        return ResponseEntity.ok(
                orderService.getOrdersByStatusCategoryAndPeriod(
                        status, categoryId, startDate, endDate));
    }
    // Dans CategoryController.java — ajouter :

    @GetMapping("/sales-report")
    public ResponseEntity<List<CategorySalesReportResponse>> getSalesReport(
            @RequestParam(defaultValue = "DELIVERED") OrderStatus status) {
        return ResponseEntity.ok(categoryService.getCategorySalesReport(status));
    }
}
