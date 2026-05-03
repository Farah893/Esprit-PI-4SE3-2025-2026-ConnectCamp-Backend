package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.dto.response.OrderStatisticsResponse;
import tn.esprit.projetintegre.entities.*;
import tn.esprit.projetintegre.enums.OrderStatus;
import tn.esprit.projetintegre.enums.PaymentStatus;
import tn.esprit.projetintegre.exception.InsufficientStockException;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.OrderRepository;
import tn.esprit.projetintegre.repositories.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final UserService userService;
    private final TrackingApiService trackingApiService;
    private final TrackingNumberGenerator trackingNumberGenerator;

    // =====================================================
    // GET ORDERS
    // =====================================================

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Page<Order> getOrdersByUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
    }

    // =====================================================
    // CREATE ORDER + TRACKING AUTO
    // =====================================================

    @Transactional
    public Order createOrderFromCart(Long userId, String shippingName, String shippingPhone,
                                     String shippingAddress, String shippingCity,
                                     String shippingPostalCode, String shippingCountry,
                                     String paymentMethod, String notes) {

        User user = userService.getUserById(userId);
        Cart cart = cartService.getCartByUserId(userId);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // ── Build order items ────────────────────────────────
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            BigDecimal itemPrice = cartItem.getPrice() != null
                    ? cartItem.getPrice()
                    : product.getPrice();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName());
            }

            BigDecimal lineTotal = itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .productThumbnail(product.getThumbnail())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(itemPrice)
                    .totalPrice(lineTotal)
                    .build();

            orderItems.add(orderItem);
            subtotal = subtotal.add(lineTotal);

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            product.setSalesCount(product.getSalesCount() + cartItem.getQuantity());
            productRepository.save(product);
        }

        // ── Calculs montants ─────────────────────────────────
        BigDecimal shippingCost   = calculateShippingCost(subtotal);
        BigDecimal taxAmount      = subtotal.multiply(BigDecimal.valueOf(0.19));
        BigDecimal discountAmount = cart.getDiscountAmount() != null
                ? cart.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount    = subtotal.add(shippingCost).add(taxAmount).subtract(discountAmount);

        // ── Sauvegarde initiale ──────────────────────────────
        Order order = Order.builder()
                .user(user)
                .subtotal(subtotal)
                .shippingCost(shippingCost)
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod(paymentMethod)
                .shippingName(shippingName)
                .shippingPhone(shippingPhone)
                .shippingAddress(shippingAddress)
                .shippingCity(shippingCity)
                .shippingPostalCode(shippingPostalCode)
                .shippingCountry(shippingCountry)
                .couponCode(cart.getAppliedCouponCode())
                .notes(notes)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Lier les items à la commande
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
        }
        savedOrder.setItems(orderItems);
        savedOrder = orderRepository.save(savedOrder);

        // ── Tracking 17TRACK ─────────────────────────────────
        // Non bloquant : la commande est créée même si 17TRACK échoue
        savedOrder = generateAndRegisterTracking(savedOrder, shippingCountry);

        // ── Post-traitement ───────────────────────────────────
        cartService.clearCart(userId);

        int loyaltyPoints = totalAmount.intValue() / 10;
        userService.addLoyaltyPoints(userId, loyaltyPoints);

        log.info("✅ Commande #{} créée | tracking: {} | total: {} €",
                savedOrder.getId(), savedOrder.getTrackingNumber(), totalAmount);

        return savedOrder;
    }

    /**
     * Génère un numéro de tracking, l'enregistre sur 17TRACK,
     * et met à jour la commande en base.
     * Retourne la commande mise à jour (ou inchangée si erreur).
     */
    private Order generateAndRegisterTracking(Order order, String shippingCountry) {
        try {
            // 1. Générer le numéro de tracking
            String trackingNumber = trackingNumberGenerator.generateAndRegisterTracking(
                    order,
                    "Colissimo",
                    shippingCountry
            );

            if (trackingNumber == null || trackingNumber.isBlank()) {
                log.warn("Tracking number généré vide pour commande #{}", order.getId());
                return order;
            }

            // 2. Persister le tracking number sur la commande
            order.setTrackingNumber(trackingNumber);
            order.setCarrierName("Colissimo");
            order = orderRepository.save(order);

            // 3. Le register 17TRACK a déjà été fait dans TrackingNumberGenerator
            //    → le numéro est maintenant visible sur dashboard.17track.net
            log.info("🚀 Tracking {} enregistré sur 17TRACK pour commande #{}",
                    trackingNumber, order.getId());

        } catch (Exception e) {
            log.error("❌ Tracking non généré pour commande #{} (commande OK): {}",
                    order.getId(), e.getMessage());
        }
        return order;
    }

    // =====================================================
    // UPDATE STATUS
    // =====================================================

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);

        switch (status) {
            case SHIPPED    -> order.setShippedAt(LocalDateTime.now());
            case DELIVERED  -> order.setDeliveredAt(LocalDateTime.now());
            case CANCELLED  -> {
                order.setCancelledAt(LocalDateTime.now());
                // Remettre le stock
                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        return orderRepository.save(order);
    }

    // =====================================================
    // PAYMENT
    // =====================================================

    @Transactional
    public Order updatePaymentStatus(Long orderId, PaymentStatus status, String transactionId) {
        Order order = getOrderById(orderId);
        order.setPaymentStatus(status);
        order.setPaymentTransactionId(transactionId);

        if (status == PaymentStatus.COMPLETED) {
            order.setPaidAt(LocalDateTime.now());
            order.setStatus(OrderStatus.CONFIRMED);
        }

        return orderRepository.save(order);
    }

    // =====================================================
    // BUSINESS LOGIC
    // =====================================================

    private BigDecimal calculateShippingCost(BigDecimal subtotal) {
        if (subtotal.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(7);
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = orderRepository.getTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    // ── NOUVEAU 1 : Statistiques globales par statut ──────────────────────────
// Appelle la query JPQL avec JOIN Order ↔ OrderItem
// Utile pour le dashboard admin : répartition des commandes par statut
    public List<OrderStatisticsResponse> getOrderStatisticsByStatus() {
        return orderRepository.getOrderStatisticsByStatus();
    }

    // ── NOUVEAU 2 : Historique filtré pour un utilisateur ────────────────────
// Combine userId + statut + date minimale de création
// Exemple : toutes les commandes DELIVERED de l'user 5 depuis 30 jours
    public List<Order> getOrdersByUserAndStatusSince(Long userId,
                                                     OrderStatus status,
                                                     LocalDateTime since) {
        return orderRepository.findByUserIdAndStatusAndCreatedAtAfter(userId, status, since);
    }
    // Ajouter dans OrderService.java :

    /**
     * Retourne les commandes d'un statut donné, filtrées par catégorie de produit
     * et par fenêtre temporelle.
     *
     * Exemple d'usage : toutes les commandes DELIVERED contenant un produit
     * de la catégorie "Électronique" sur le dernier trimestre.
     *
     * @param status     statut de commande (ex: DELIVERED)
     * @param categoryId identifiant de la catégorie cible
     * @param startDate  borne inférieure (incluse)
     * @param endDate    borne supérieure (incluse)
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatusCategoryAndPeriod(
            OrderStatus status,
            Long categoryId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        // Valeurs par défaut si non fournies
        LocalDateTime from = (startDate != null) ? startDate : LocalDateTime.now().minusMonths(1);
        LocalDateTime to   = (endDate   != null) ? endDate   : LocalDateTime.now();

        return orderRepository
                .findDistinctByStatusAndItems_Product_Category_IdAndCreatedAtBetween(
                        status, categoryId, from, to);
    }
}