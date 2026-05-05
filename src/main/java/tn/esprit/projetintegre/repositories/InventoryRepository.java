package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Inventory;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Override
    @EntityGraph(attributePaths = {"product", "warehouse"}) // Charge les relations nécessaires
    Optional<Inventory> findById(Long id);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    List<Inventory> findByProductId(Long productId);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    List<Inventory> findByWarehouseId(Long warehouseId);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    Optional<Inventory> findBySku(String sku);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.lowStockThreshold")
    List<Inventory> findLowStockItems();

    @EntityGraph(attributePaths = {"product", "warehouse"})
    @Query("SELECT i FROM Inventory i WHERE i.warehouse.id = :warehouseId AND i.quantity <= i.lowStockThreshold")
    List<Inventory> findLowStockByWarehouse(Long warehouseId);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalStockByProduct(Long productId);
}
