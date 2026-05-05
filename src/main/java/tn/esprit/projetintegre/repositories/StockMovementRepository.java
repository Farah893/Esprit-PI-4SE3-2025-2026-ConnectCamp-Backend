package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.StockMovement;
import tn.esprit.projetintegre.enums.MovementType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Override
    @EntityGraph(attributePaths = {"product", "warehouse", "performedBy"})
    Optional<StockMovement> findById(Long id);

    @EntityGraph(attributePaths = {"product", "warehouse", "performedBy"})
    Optional<StockMovement> findByReference(String reference);

    @EntityGraph(attributePaths = {"product", "warehouse", "performedBy"})
    List<StockMovement> findByProductId(Long productId);

    @EntityGraph(attributePaths = {"product", "warehouse", "performedBy"})
    Page<StockMovement> findByProductId(Long productId, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "warehouse", "performedBy"})
    List<StockMovement> findByWarehouseId(Long warehouseId);

    @EntityGraph(attributePaths = {"product", "warehouse", "performedBy"})
    List<StockMovement> findByMovementType(MovementType type);

    @EntityGraph(attributePaths = {"product", "warehouse", "performedBy"})
    @Query("SELECT m FROM StockMovement m WHERE m.movementDate BETWEEN :start AND :end")
    List<StockMovement> findByDateRange(LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"product", "warehouse", "performedBy"})
    List<StockMovement> findByPerformedById(Long userId);
}
