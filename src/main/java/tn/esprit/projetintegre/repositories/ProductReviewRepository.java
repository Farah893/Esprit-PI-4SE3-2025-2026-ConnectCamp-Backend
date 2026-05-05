package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ProductReview;

import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    @Override
    @EntityGraph(attributePaths = {"product", "user"}) // Charge les relations nécessaires
    Optional<ProductReview> findById(Long id);

    @EntityGraph(attributePaths = {"product", "user"})
    Page<ProductReview> findByProductId(Long productId, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "user"})
    Page<ProductReview> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "user"})
    Optional<ProductReview> findByProductIdAndUserId(Long productId, Long userId);

    @EntityGraph(attributePaths = {"product", "user"})
    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId AND r.isApproved = true")
    Double getAverageRatingForProduct(Long productId);

    @EntityGraph(attributePaths = {"product", "user"})
    long countByProductIdAndIsApprovedTrue(Long productId);
}
