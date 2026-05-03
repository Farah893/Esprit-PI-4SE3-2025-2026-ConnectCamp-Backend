package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.dto.response.OrderStatisticsResponse;
import tn.esprit.projetintegre.entities.Order;
import tn.esprit.projetintegre.enums.OrderStatus;
import tn.esprit.projetintegre.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<Order> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {"user"})
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal getTotalRevenue();

    // ── Scheduler ────────────────────────────────────────────────────────────
    List<Order> findByStatusAndPaymentStatusAndCreatedAtBefore(
            OrderStatus status,
            PaymentStatus paymentStatus,
            LocalDateTime cutoffTime
    );

    // ── NOUVEAU 1 : Statistiques par statut (JOIN avec OrderItem) ─────────────
    // JOIN Order → OrderItem pour agréger :
    //   - nombre de commandes distinctes par statut
    //   - total articles vendus (SUM quantity)
    //   - revenu moyen par type d'article (SUM totalAmount / COUNT items distincts)
    // SELECT NEW projette directement dans le DTO → pas de Object[] à parser
    @Query("SELECT new tn.esprit.projetintegre.dto.response.OrderStatisticsResponse(" +
            "   CAST(o.status AS string), " +
            "   COUNT(DISTINCT o.id), " +
            "   COALESCE(SUM(oi.quantity), 0L), " +
            "   COALESCE(SUM(o.totalAmount) / NULLIF(COUNT(DISTINCT oi.id), 0), 0) " +
            ") " +
            "FROM Order o " +
            "LEFT JOIN o.items oi " +
            "GROUP BY o.status " +
            "ORDER BY COUNT(DISTINCT o.id) DESC")
    List<OrderStatisticsResponse> getOrderStatisticsByStatus();

    // ── NOUVEAU 2 : Filtre multi-critères user + statut + date ────────────────
    // Spring Data génère automatiquement :
    //   SELECT * FROM orders
    //   WHERE user_id = ?1 AND status = ?2 AND created_at > ?3
    // @EntityGraph pour éviter le N+1 sur les items
    @EntityGraph(attributePaths = {"items"})
    List<Order> findByUserIdAndStatusAndCreatedAtAfter(
            Long userId,
            OrderStatus status,
            LocalDateTime createdAfter
    );

    // Ajouter dans OrderRepository.java :

    /**
     * Keyword-based query — Spring Data génère automatiquement :
     *
     *   SELECT DISTINCT o.*
     *   FROM orders o
     *   JOIN order_items oi  ON oi.order_id   = o.id
     *   JOIN products p      ON p.id          = oi.product_id
     *   JOIN categories cat  ON cat.id        = p.category_id
     *   WHERE o.status              = :status
     *     AND cat.id                = :categoryId
     *     AND o.created_at BETWEEN :startDate AND :endDate
     *
     * Traverse 4 tables (Order, OrderItem, Product, Category)
     * grâce aux associations JPA déclarées dans les entités.
     *
     * @EntityGraph évite le N+1 sur items + product + category au chargement.
     */
    @EntityGraph(attributePaths = {"items", "items.product", "items.product.category", "user"})
    List<Order> findDistinctByStatusAndItems_Product_Category_IdAndCreatedAtBetween(
            OrderStatus status,
            Long categoryId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

}