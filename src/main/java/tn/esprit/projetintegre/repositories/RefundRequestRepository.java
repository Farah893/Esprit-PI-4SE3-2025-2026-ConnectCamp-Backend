package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.RefundRequest;
import tn.esprit.projetintegre.enums.RefundStatus;
import tn.esprit.projetintegre.enums.RefundRequestType;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "order"}) // Charge les relations nécessaires
    Optional<RefundRequest> findById(Long id);

    @EntityGraph(attributePaths = {"user", "order"})
    Optional<RefundRequest> findByRequestNumber(String number);

    @EntityGraph(attributePaths = {"user", "order"})
    List<RefundRequest> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "order"})
    Page<RefundRequest> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "order"})
    List<RefundRequest> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"user", "order"})
    List<RefundRequest> findByStatus(RefundStatus status);

    @EntityGraph(attributePaths = {"user", "order"})
    Page<RefundRequest> findByStatus(RefundStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "order"})
    List<RefundRequest> findByRequestType(RefundRequestType type);
}
