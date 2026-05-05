package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.OrderStatusHistory;
import tn.esprit.projetintegre.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    @Override
    @EntityGraph(attributePaths = {"order", "changedBy"}) // Charge les relations nécessaires
    Optional<OrderStatusHistory> findById(Long id);

    @EntityGraph(attributePaths = {"order", "changedBy"})
    List<OrderStatusHistory> findByOrderIdOrderByChangedAtDesc(Long orderId);

    @EntityGraph(attributePaths = {"order", "changedBy"})
    List<OrderStatusHistory> findByStatus(OrderStatus status);

    @EntityGraph(attributePaths = {"order", "changedBy"})
    List<OrderStatusHistory> findByChangedById(Long userId);

    @EntityGraph(attributePaths = {"order", "changedBy"})
    List<OrderStatusHistory> findByNotificationSent(Boolean sent);
}
