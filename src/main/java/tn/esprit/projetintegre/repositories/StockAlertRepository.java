package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.StockAlert;
import tn.esprit.projetintegre.enums.AlertType;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    @Override
    @EntityGraph(attributePaths = {"product", "warehouse"})
    Optional<StockAlert> findById(Long id);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    List<StockAlert> findByProductId(Long productId);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    List<StockAlert> findByWarehouseId(Long warehouseId);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    List<StockAlert> findByIsResolved(Boolean resolved);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    Page<StockAlert> findByIsResolved(Boolean resolved, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    List<StockAlert> findByAlertType(AlertType type);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    List<StockAlert> findByProductIdAndIsResolved(Long productId, Boolean resolved);
}
