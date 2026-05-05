package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ProductVariant;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Override
    @EntityGraph(attributePaths = {"product"}) // Charge le produit associé aux variantes
    Optional<ProductVariant> findById(Long id);

    @EntityGraph(attributePaths = {"product"})
    Optional<ProductVariant> findBySku(String sku);

    @EntityGraph(attributePaths = {"product"})
    List<ProductVariant> findByProductId(Long productId);

    @EntityGraph(attributePaths = {"product"})
    List<ProductVariant> findByProductIdAndIsActive(Long productId, Boolean isActive);

    @EntityGraph(attributePaths = {"product"})
    Optional<ProductVariant> findByProductIdAndIsDefault(Long productId, Boolean isDefault);

    @EntityGraph(attributePaths = {"product"})
    List<ProductVariant> findByColor(String color);

    @EntityGraph(attributePaths = {"product"})
    List<ProductVariant> findBySize(String size);

    boolean existsBySku(String sku);

    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.stock > 0 AND v.isActive = true")
    List<ProductVariant> findAvailableVariants(Long productId);
}
