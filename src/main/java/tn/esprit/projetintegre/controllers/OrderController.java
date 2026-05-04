package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.PageResponse;
import tn.esprit.projetintegre.dto.request.TrackingUpdateRequest;
import tn.esprit.projetintegre.dto.response.InvoiceDTO;
import tn.esprit.projetintegre.dto.response.OrderResponse;
import tn.esprit.projetintegre.dto.response.OrderStatisticsResponse;
import tn.esprit.projetintegre.dto.response.TrackingResponse;
import tn.esprit.projetintegre.entities.Order;
import tn.esprit.projetintegre.enums.OrderStatus;
import tn.esprit.projetintegre.enums.PaymentStatus;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.InvoiceService;
import tn.esprit.projetintegre.services.OrderService;
import tn.esprit.projetintegre.services.TrackingService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class OrderController {

    private final OrderService orderService;
    private final DtoMapper dtoMapper;
    private final InvoiceService invoiceService;
    private final TrackingService trackingService;

    // =====================================================
    // ORDERS
    // =====================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(
                ApiResponse.success(dtoMapper.toOrderResponseList(orderService.getAllOrders()))
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrdersByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Order> orders = orderService.getOrdersByUser(userId, PageRequest.of(page, size));

        return ResponseEntity.ok(
                ApiResponse.success(PageResponse.from(orders.map(dtoMapper::toOrderResponse)))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(dtoMapper.toOrderResponse(orderService.getOrderById(id)))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestParam Long userId,
            @RequestParam String shippingName,
            @RequestParam String shippingPhone,
            @RequestParam String shippingAddress,
            @RequestParam String shippingCity,
            @RequestParam String shippingPostalCode,
            @RequestParam String shippingCountry,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String notes) {

        Order order = orderService.createOrderFromCart(
                userId, shippingName, shippingPhone,
                shippingAddress, shippingCity,
                shippingPostalCode, shippingCountry,
                paymentMethod, notes
        );

        return ResponseEntity.ok(ApiResponse.success("Order created", dtoMapper.toOrderResponse(order)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        return ResponseEntity.ok(
                ApiResponse.success(dtoMapper.toOrderResponse(orderService.updateOrderStatus(id, status)))
        );
    }

    @PatchMapping("/{id}/payment")
    public ResponseEntity<ApiResponse<OrderResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus status,
            @RequestParam(required = false) String transactionId) {

        return ResponseEntity.ok(
                ApiResponse.success(dtoMapper.toOrderResponse(
                        orderService.updatePaymentStatus(id, status, transactionId)))
        );
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalRevenue() {
        return ResponseEntity.ok(ApiResponse.success(orderService.getTotalRevenue()));
    }

    // =====================================================
    // INVOICES
    // =====================================================

    @GetMapping("/{id}/invoice")
    public ResponseEntity<ApiResponse<InvoiceDTO>> getOrderInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.generateInvoice(id)));
    }

    @GetMapping("/{id}/invoice/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) throws IOException {

        InvoiceDTO invoice = invoiceService.generateInvoice(id);
        File file = invoiceService.getPdfFile(invoice.getPdfUrl());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + invoice.getInvoiceNumber() + ".pdf\"")
                .body(Files.readAllBytes(file.toPath()));
    }

    @GetMapping("/invoices/user/{userId}")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getUserInvoices(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getInvoicesByUser(userId)));
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getOrderInvoices(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getInvoicesByOrder(id)));
    }

    @PostMapping("/{id}/invoice/regenerate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<InvoiceDTO>> regenerateInvoice(@PathVariable Long id) {

        List<InvoiceDTO> existing = invoiceService.getInvoicesByOrder(id);

        if (existing.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    "Invoice created",
                    invoiceService.generateInvoice(id)
            ));
        }

        return ResponseEntity.ok(ApiResponse.success(
                "PDF regenerated",
                invoiceService.regeneratePdf(existing.get(0).getId())
        ));
    }

    // =====================================================
    // TRACKING
    // =====================================================

    @GetMapping("/{id}/tracking")
    public ResponseEntity<ApiResponse<TrackingResponse>> getOrderTracking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(trackingService.getTracking(id)));
    }

    @GetMapping("/tracking/{orderNumber}")
    public ResponseEntity<ApiResponse<TrackingResponse>> getTrackingByOrderNumber(
            @PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                trackingService.getTrackingByOrderNumber(orderNumber)));
    }

    @PostMapping("/{id}/tracking")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<ApiResponse<TrackingResponse>> addTrackingEvent(
            @PathVariable Long id,
            @RequestBody TrackingUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Tracking updated",
                trackingService.addTrackingEvent(id, request)));
    }

    @GetMapping("/user/{userId}/tracking")
    public ResponseEntity<ApiResponse<List<TrackingResponse>>> getUserTracking(
            @PathVariable Long userId) {

        return ResponseEntity.ok(ApiResponse.success(
                trackingService.getTrackingByUser(userId)));
    }
    // ── NOUVEAU 1 : GET /api/orders/statistics/by-status ─────────────────────
// Retourne les stats (count, items vendus, revenu moyen) groupées par statut
// Réservé ADMIN/SELLER — utilisé pour les dashboards analytiques
    @GetMapping("/statistics/by-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Order statistics grouped by status")
    public ResponseEntity<ApiResponse<List<OrderStatisticsResponse>>> getOrderStatisticsByStatus() {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.getOrderStatisticsByStatus())
        );
    }

    // ── NOUVEAU 2 : GET /api/orders/user/{userId}/filter ─────────────────────
// Filtre les commandes d'un user par statut ET date minimale
// Exemple : GET /api/orders/user/5/filter?status=DELIVERED&since=2025-01-01T00:00:00
    @GetMapping("/user/{userId}/filter")
    @Operation(summary = "Filter user orders by status and date")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getFilteredUserOrders(
            @PathVariable Long userId,
            @RequestParam OrderStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        List<Order> orders = orderService.getOrdersByUserAndStatusSince(userId, status, since);
        return ResponseEntity.ok(
                ApiResponse.success(dtoMapper.toOrderResponseList(orders))
        );
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
}