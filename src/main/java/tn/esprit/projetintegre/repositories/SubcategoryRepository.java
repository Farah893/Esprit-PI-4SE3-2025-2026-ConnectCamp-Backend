package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Subcategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {

    @Override
    @EntityGraph(attributePaths = {"category"})
    Optional<Subcategory> findById(Long id);

    @EntityGraph(attributePaths = {"category"})
    List<Subcategory> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category"})
    Optional<Subcategory> findBySlug(String slug);

    @EntityGraph(attributePaths = {"category"})
    List<Subcategory> findByIsActive(Boolean isActive);

    @EntityGraph(attributePaths = {"category"})
    List<Subcategory> findByIsFeatured(Boolean isFeatured);

    @EntityGraph(attributePaths = {"category"})
    List<Subcategory> findByCategoryIdAndIsActive(Long categoryId, Boolean isActive);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"category"})
    List<Subcategory> findByCategoryIdOrderByDisplayOrderAsc(Long categoryId);
}
