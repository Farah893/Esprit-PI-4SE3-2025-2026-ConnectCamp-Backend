package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Review;
import tn.esprit.projetintegre.enums.ReviewTargetType;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"}) // Charge l'utilisateur qui a écrit l'avis
    Optional<Review> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    List<Review> findByTargetTypeAndTargetId(ReviewTargetType targetType, Long targetId);

    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByTargetTypeAndTargetId(ReviewTargetType targetType, Long targetId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    List<Review> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Optional<Review> findByUserIdAndTargetTypeAndTargetId(Long userId, ReviewTargetType targetType, Long targetId);

    @EntityGraph(attributePaths = {"user"})
    List<Review> findByApproved(Boolean approved);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.targetType = :targetType AND r.targetId = :targetId AND r.approved = true")
    Double getAverageRating(ReviewTargetType targetType, Long targetId);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT COUNT(r) FROM Review r WHERE r.targetType = :targetType AND r.targetId = :targetId AND r.approved = true")
    Long countApprovedReviews(ReviewTargetType targetType, Long targetId);
}
