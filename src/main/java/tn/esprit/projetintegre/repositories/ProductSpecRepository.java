package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ProductSpec;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {

    @Override
    @EntityGraph(attributePaths = {"product"}) // Charge le produit associé aux spécifications
    Optional<ProductSpec> findById(Long id);

    @EntityGraph(attributePaths = {"product"})
    List<ProductSpec> findByProductId(Long productId);

    @EntityGraph(attributePaths = {"product"})
    List<ProductSpec> findByProductIdAndIsVisible(Long productId, Boolean isVisible);

    @EntityGraph(attributePaths = {"product"})
    List<ProductSpec> findByProductIdAndIsFilterable(Long productId, Boolean isFilterable);

    @EntityGraph(attributePaths = {"product"})
    List<ProductSpec> findBySpecGroup(String group);

    @EntityGraph(attributePaths = {"product"})
    List<ProductSpec> findByProductIdOrderByDisplayOrderAsc(Long productId);
}
