package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {"category", "seller", "images"})
    Optional<Product> findById(Long id);

    @EntityGraph(attributePaths = {"category", "seller", "images"})
    Page<Product> findByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "seller", "images"})
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "seller", "images"})
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "seller", "images"})
    List<Product> findByIsFeaturedTrue();

    @EntityGraph(attributePaths = {"category", "seller", "images"})
    List<Product> findByIsOnSaleTrue();

    @EntityGraph(attributePaths = {"category", "seller", "images"})
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "seller", "images"})
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "seller", "images"})
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.salesCount DESC")
    List<Product> findTopSellingProducts(Pageable pageable);
    // ProductRepository.java
    List<Product> findByCategoryId(Long categoryId);
    @EntityGraph(attributePaths = {"category", "seller", "images"})
    Page<Product> findByIsActiveTrueAndStockQuantityLessThan(int threshold, Pageable pageable);

    List<Product> findTop50ByIsActiveTrue();

}
