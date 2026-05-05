package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.OrderItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Override
    @EntityGraph(attributePaths = {"order", "product"}) // Charge les relations nécessaires
    Optional<OrderItem> findById(Long id);

    @EntityGraph(attributePaths = {"order", "product"})
    List<OrderItem> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"order", "product"})
    List<OrderItem> findByProductId(Long productId);

    @EntityGraph(attributePaths = {"order", "product"})
    void deleteByOrderId(Long orderId);
}
