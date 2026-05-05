package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.dto.response.CategorySalesReportResponse;
import tn.esprit.projetintegre.entities.Category;
import tn.esprit.projetintegre.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @EntityGraph(attributePaths = {"products"})
    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    @EntityGraph(attributePaths = {"products"})
    Optional<Category> findBySlug(String slug);

    @EntityGraph(attributePaths = {"products"})
    List<Category> findAll();

    @EntityGraph(attributePaths = {"products"})
    List<Category> findByIsActiveTrue();

    List<Category> findByParentIsNull();

    List<Category> findByParentId(Long parentId);

    boolean existsByName(String name);

    @Query("""
    SELECT new tn.esprit.projetintegre.dto.response.CategorySalesReportResponse(
        c.id,
        c.name,
        COUNT(DISTINCT o.id),
        COALESCE(SUM(oi.quantity), 0L),
        COALESCE(SUM(oi.totalPrice), 0),
        CASE WHEN COUNT(DISTINCT o.id) = 0 THEN 0
             ELSE SUM(oi.totalPrice) / COUNT(DISTINCT o.id) END
    )
    FROM Category c
    JOIN c.products p
    JOIN OrderItem oi ON oi.product = p
    JOIN oi.order o
    WHERE o.status = :status
      AND c.isActive = true
    GROUP BY c.id, c.name
    ORDER BY SUM(oi.totalPrice) DESC
    """)
    List<CategorySalesReportResponse> getCategorySalesReport(
            @Param("status") OrderStatus status
    );
}
