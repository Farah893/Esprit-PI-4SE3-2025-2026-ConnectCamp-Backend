package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.CouponUsage;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    @Override
    @EntityGraph(attributePaths = {"coupon", "user", "order"}) // Charge les relations nécessaires
    Optional<CouponUsage> findById(Long id);

    @EntityGraph(attributePaths = {"coupon", "user", "order"})
    List<CouponUsage> findByCouponId(Long couponId);

    @EntityGraph(attributePaths = {"coupon", "user", "order"})
    List<CouponUsage> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"coupon", "user", "order"})
    Page<CouponUsage> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"coupon", "user", "order"})
    List<CouponUsage> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"coupon", "user", "order"})
    long countByCouponId(Long couponId);

    @EntityGraph(attributePaths = {"coupon", "user", "order"})
    long countByUserIdAndCouponId(Long userId, Long couponId);

    @EntityGraph(attributePaths = {"coupon", "user", "order"})
    @Query("SELECT SUM(c.discountAmount) FROM CouponUsage c WHERE c.coupon.id = :couponId")
    Double getTotalDiscountByCoupon(Long couponId);
}
