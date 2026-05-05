package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ServiceReview;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceReviewRepository extends JpaRepository<ServiceReview, Long> {

    @Override
    @EntityGraph(attributePaths = {"service", "user"})
    Optional<ServiceReview> findById(Long id);

    @EntityGraph(attributePaths = {"service", "user"})
    Page<ServiceReview> findByServiceId(Long serviceId, Pageable pageable);

    @EntityGraph(attributePaths = {"service", "user"})
    Page<ServiceReview> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"service", "user"})
    Optional<ServiceReview> findByServiceIdAndUserId(Long serviceId, Long userId);

    @EntityGraph(attributePaths = {"service", "user"})
    Page<ServiceReview> findByServiceIdAndIsApprovedTrue(Long serviceId, Pageable pageable);

    @EntityGraph(attributePaths = {"service", "user"})
    @Query("SELECT AVG(sr.rating) FROM ServiceReview sr WHERE sr.service.id = :serviceId AND sr.isApproved = true")
    Double getAverageRatingByServiceId(@Param("serviceId") Long serviceId);

    @EntityGraph(attributePaths = {"service", "user"})
    @Query("SELECT COUNT(sr) FROM ServiceReview sr WHERE sr.service.id = :serviceId AND sr.isApproved = true")
    Long countApprovedReviewsByServiceId(@Param("serviceId") Long serviceId);

    @EntityGraph(attributePaths = {"service", "user"})
    @Query("SELECT sr FROM ServiceReview sr WHERE sr.service.id = :serviceId AND sr.isApproved = true ORDER BY sr.helpfulCount DESC")
    Page<ServiceReview> findMostHelpfulByServiceId(@Param("serviceId") Long serviceId, Pageable pageable);

    @EntityGraph(attributePaths = {"service", "user"})
    @Query("SELECT sr FROM ServiceReview sr WHERE sr.service.id = :serviceId AND sr.rating >= :minRating AND sr.isApproved = true")
    Page<ServiceReview> findByServiceIdAndMinRating(@Param("serviceId") Long serviceId, @Param("minRating") Integer minRating, Pageable pageable);

    @EntityGraph(attributePaths = {"service", "user"})
    @Query("SELECT sr FROM ServiceReview sr WHERE sr.isApproved = false ORDER BY sr.createdAt DESC")
    Page<ServiceReview> findPendingReviews(Pageable pageable);

    @Query("SELECT sr.comment FROM ServiceReview sr WHERE sr.service.id = :serviceId AND sr.isApproved = true")
    List<String> findAllCommentsByServiceId(@Param("serviceId") Long serviceId);
}
