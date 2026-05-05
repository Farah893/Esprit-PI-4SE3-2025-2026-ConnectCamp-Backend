package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Refund;
import tn.esprit.projetintegre.enums.PaymentStatus;

import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "order"}) // Charge les relations nécessaires
    Optional<Refund> findById(Long id);

    @EntityGraph(attributePaths = {"user", "order"})
    Optional<Refund> findByRefundNumber(String refundNumber);

    @EntityGraph(attributePaths = {"user", "order"})
    Page<Refund> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "order"})
    Page<Refund> findByOrderId(Long orderId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "order"})
    Page<Refund> findByStatus(PaymentStatus status, Pageable pageable);
}
