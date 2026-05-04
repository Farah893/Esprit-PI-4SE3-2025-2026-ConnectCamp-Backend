package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Coupon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findById(Long id);

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    List<Coupon> findByIsActiveTrue();

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND (c.validFrom IS NULL OR c.validFrom <= :now) AND (c.validUntil IS NULL OR c.validUntil >= :now)")
    List<Coupon> findValidCoupons(LocalDateTime now);
}