package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.PromotionUsage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {

    @Override
    @EntityGraph(attributePaths = {"promotion", "user", "order"}) // Charge les relations nécessaires
    Optional<PromotionUsage> findById(Long id);

    @EntityGraph(attributePaths = {"promotion", "user", "order"})
    Page<PromotionUsage> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"promotion", "user", "order"})
    Page<PromotionUsage> findByPromotionId(Long promotionId, Pageable pageable);

    @EntityGraph(attributePaths = {"promotion", "user", "order"})
    Optional<PromotionUsage> findByPromotionIdAndOrderId(Long promotionId, Long orderId);

    @EntityGraph(attributePaths = {"promotion", "user", "order"})
    @Query("SELECT COUNT(pu) FROM PromotionUsage pu WHERE pu.promotion.id = :promotionId")
    Long countByPromotionId(@Param("promotionId") Long promotionId);

    @EntityGraph(attributePaths = {"promotion", "user", "order"})
    @Query("SELECT COUNT(pu) FROM PromotionUsage pu WHERE pu.user.id = :userId AND pu.promotion.id = :promotionId")
    Long countByUserIdAndPromotionId(@Param("userId") Long userId, @Param("promotionId") Long promotionId);

    @EntityGraph(attributePaths = {"promotion", "user", "order"})
    @Query("SELECT SUM(pu.discountAmount) FROM PromotionUsage pu WHERE pu.promotion.id = :promotionId")
    BigDecimal getTotalDiscountByPromotion(@Param("promotionId") Long promotionId);

    @EntityGraph(attributePaths = {"promotion", "user", "order"})
    @Query("SELECT pu FROM PromotionUsage pu WHERE pu.usedAt >= :startDate AND pu.usedAt <= :endDate")
    List<PromotionUsage> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
