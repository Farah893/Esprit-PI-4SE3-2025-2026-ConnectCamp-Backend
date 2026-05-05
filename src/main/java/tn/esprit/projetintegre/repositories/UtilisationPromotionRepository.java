package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.UtilisationPromotion;

import java.util.List;

@Repository
public interface UtilisationPromotionRepository extends JpaRepository<UtilisationPromotion, Long> {

    @EntityGraph(attributePaths = {"user", "promotion", "order"})
    List<UtilisationPromotion> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "promotion", "order"})
    Page<UtilisationPromotion> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "promotion", "order"})
    List<UtilisationPromotion> findByPromotionId(Long promotionId);

    @EntityGraph(attributePaths = {"user", "promotion", "order"})
    List<UtilisationPromotion> findByOrderId(Long orderId);

    long countByPromotionId(Long promotionId);
    long countByUserIdAndPromotionId(Long userId, Long promotionId);

    @Query("SELECT SUM(u.montantReduction) FROM UtilisationPromotion u WHERE u.promotion.id = :promotionId")
    Double getTotalDiscountByPromotion(Long promotionId);
}
