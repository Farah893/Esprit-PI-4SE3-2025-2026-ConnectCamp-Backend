package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.dto.PackServiceStatsDTO;
import tn.esprit.projetintegre.dto.PackOptimizerDTO;
import tn.esprit.projetintegre.dto.PackQualityDTO;
import tn.esprit.projetintegre.entities.Pack;
import tn.esprit.projetintegre.enums.PackType;
import tn.esprit.projetintegre.enums.ServiceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import tn.esprit.projetintegre.repositories.projections.PackProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackRepository extends JpaRepository<Pack, Long> {

    @Override
    @EntityGraph(attributePaths = { "site" })
    Optional<Pack> findById(Long id);

    @Modifying
    @Query("UPDATE Pack p SET p.isActive = :active WHERE p.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("active") boolean active);

    @Modifying
    @Query(value = "DELETE FROM pack_services WHERE pack_id = :id", nativeQuery = true)
    void deletePackServices(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM pack_images WHERE pack_id = :id", nativeQuery = true)
    void deletePackImages(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM pack_features WHERE pack_id = :id", nativeQuery = true)
    void deletePackFeatures(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM pack_inclusions WHERE pack_id = :id", nativeQuery = true)
    void deletePackInclusions(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM pack_exclusions WHERE pack_id = :id", nativeQuery = true)
    void deletePackExclusions(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM packs WHERE id = :id", nativeQuery = true)
    void deletePackById(@Param("id") Long id);

    @Query("SELECT p.id as id, p.name as name, p.description as description, p.packType as packType, " +
           "p.price as price, p.originalPrice as originalPrice, " +
           "p.durationDays as durationDays, p.maxPersons as maxPersons, p.isActive as isActive, " +
           "p.isFeatured as isFeatured, p.isLimitedOffer as isLimitedOffer, " +
           "p.availableQuantity as availableQuantity, p.soldCount as soldCount, " +
           "p.rating as rating, p.reviewCount as reviewCount, p.validFrom as validFrom, " +
           "p.validUntil as validUntil, p.createdAt as createdAt, " +
           "p.imageUrl as imageUrl, size(p.services) as serviceCount, " +
           "s.id as siteId, s.name as siteName " +
           "FROM Pack p LEFT JOIN p.site s WHERE p.id = :id")
    Optional<PackProjection> findByIdProjected(@Param("id") Long id);

    @Query("SELECT p.id as id, p.name as name, p.description as description, p.packType as packType, " +
           "p.price as price, p.originalPrice as originalPrice, " +
           "p.durationDays as durationDays, p.maxPersons as maxPersons, p.isActive as isActive, " +
           "p.isFeatured as isFeatured, p.isLimitedOffer as isLimitedOffer, " +
           "p.availableQuantity as availableQuantity, p.soldCount as soldCount, " +
           "p.rating as rating, p.reviewCount as reviewCount, p.validFrom as validFrom, " +
           "p.validUntil as validUntil, p.createdAt as createdAt, " +
           "p.imageUrl as imageUrl, size(p.services) as serviceCount, " +
           "s.id as siteId, s.name as siteName " +
           "FROM Pack p LEFT JOIN p.site s WHERE p.isActive = true")
    Page<PackProjection> findByIsActiveTrueProjected(Pageable pageable);

    @Query("SELECT p.id as id, p.name as name, p.description as description, p.packType as packType, " +
           "p.price as price, p.originalPrice as originalPrice, " +
           "p.durationDays as durationDays, p.maxPersons as maxPersons, p.isActive as isActive, " +
           "p.isFeatured as isFeatured, p.isLimitedOffer as isLimitedOffer, " +
           "p.availableQuantity as availableQuantity, p.soldCount as soldCount, " +
           "p.rating as rating, p.reviewCount as reviewCount, p.validFrom as validFrom, " +
           "p.validUntil as validUntil, p.createdAt as createdAt, " +
           "p.imageUrl as imageUrl, size(p.services) as serviceCount, " +
           "s.id as siteId, s.name as siteName " +
           "FROM Pack p LEFT JOIN p.site s")
    Page<PackProjection> findAllProjected(Pageable pageable);

    @Query("SELECT p.id as id, p.name as name, p.description as description, p.packType as packType, " +
           "p.price as price, p.originalPrice as originalPrice, " +
           "p.durationDays as durationDays, p.maxPersons as maxPersons, p.isActive as isActive, " +
           "p.isFeatured as isFeatured, p.isLimitedOffer as isLimitedOffer, " +
           "p.availableQuantity as availableQuantity, p.soldCount as soldCount, " +
           "p.rating as rating, p.reviewCount as reviewCount, p.validFrom as validFrom, " +
           "p.validUntil as validUntil, p.createdAt as createdAt, " +
           "p.imageUrl as imageUrl, size(p.services) as serviceCount, " +
           "s.id as siteId, s.name as siteName " +
           "FROM Pack p LEFT JOIN p.site s WHERE p.packType = :type AND p.isActive = true")
    Page<PackProjection> findByPackTypeProjected(@Param("type") PackType type, Pageable pageable);

    @Query("SELECT p.id as id, p.name as name, p.description as description, p.packType as packType, " +
           "p.price as price, p.originalPrice as originalPrice, " +
           "p.durationDays as durationDays, p.maxPersons as maxPersons, p.isActive as isActive, " +
           "p.isFeatured as isFeatured, p.isLimitedOffer as isLimitedOffer, " +
           "p.availableQuantity as availableQuantity, p.soldCount as soldCount, " +
           "p.rating as rating, p.reviewCount as reviewCount, p.validFrom as validFrom, " +
           "p.validUntil as validUntil, p.createdAt as createdAt, " +
           "p.imageUrl as imageUrl, size(p.services) as serviceCount, " +
           "s.id as siteId, s.name as siteName " +
           "FROM Pack p LEFT JOIN p.site s WHERE s.id = :siteId AND p.isActive = true")
    Page<PackProjection> findBySiteIdProjected(@Param("siteId") Long siteId, Pageable pageable);

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT p FROM Pack p WHERE p.isActive = true AND p.isFeatured = true")
    List<Pack> findFeaturedPacks();

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT p FROM Pack p WHERE p.isActive = true AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Pack> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT p FROM Pack p WHERE p.isActive = true AND p.isLimitedOffer = true AND p.validUntil > :now")
    List<Pack> findActiveLimitedOffers(@Param("now") LocalDateTime now);

    @Query("SELECT p.id as id, p.name as name, p.description as description, p.packType as packType, " +
           "p.price as price, p.originalPrice as originalPrice, " +
           "p.durationDays as durationDays, p.maxPersons as maxPersons, p.isActive as isActive, " +
           "p.isFeatured as isFeatured, p.isLimitedOffer as isLimitedOffer, " +
           "p.availableQuantity as availableQuantity, p.soldCount as soldCount, " +
           "p.rating as rating, p.reviewCount as reviewCount, p.validFrom as validFrom, " +
           "p.validUntil as validUntil, p.createdAt as createdAt, " +
           "p.imageUrl as imageUrl, size(p.services) as serviceCount, " +
           "s.id as siteId, s.name as siteName " +
           "FROM Pack p LEFT JOIN p.site s WHERE p.isActive = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PackProjection> searchPacksProjected(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT p FROM Pack p WHERE p.isActive = true AND p.maxPersons >= :persons")
    Page<Pack> findByMinPersons(@Param("persons") Integer persons, Pageable pageable);

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT p FROM Pack p WHERE p.isActive = true ORDER BY p.soldCount DESC")
    List<Pack> findTopSellingPacks(Pageable pageable);

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT p FROM Pack p WHERE p.isActive = true ORDER BY p.rating DESC")
    List<Pack> findTopRatedPacks(Pageable pageable);

    // --- Advanced Methods migrated from reference ---

    @Query("SELECT new tn.esprit.projetintegre.dto.PackServiceStatsDTO(" +
           "  p.id, p.name, p.packType, p.price, p.originalPrice, " +
           "  s.name, COUNT(cs.id), COALESCE(SUM(cs.price), 0)) " +
           "FROM Pack p " +
           "LEFT JOIN p.site s " +
           "LEFT JOIN p.services cs " +
           "WHERE p.isActive = true " +
           "GROUP BY p.id, p.name, p.packType, p.price, p.originalPrice, s.id, s.name " +
           "ORDER BY p.soldCount DESC")
    List<PackServiceStatsDTO> findActivePacksWithServiceStats();

    @Query("SELECT new tn.esprit.projetintegre.dto.PackOptimizerDTO(" +
           "  p.id, p.name, p.packType, p.price, " +
           "  s.name, COUNT(cs.id), COALESCE(SUM(cs.price), 0), p.maxPersons) " +
           "FROM Pack p " +
           "LEFT JOIN p.site s " +
           "LEFT JOIN p.services cs " +
           "WHERE p.isActive = true " +
           "GROUP BY p.id, p.name, p.packType, p.price, s.id, s.name, p.maxPersons " +
           "ORDER BY p.soldCount DESC")
    List<PackOptimizerDTO> findPacksForOptimizer();

    @Query("SELECT p FROM Pack p JOIN p.site s JOIN p.services cs WHERE p.isActive = true AND LOWER(s.name) LIKE LOWER(CONCAT('%', :siteName, '%')) AND cs.type = :serviceType")
    List<Pack> findByIsActiveTrueAndSite_NameContainingIgnoreCaseAndServices_Type(@Param("siteName") String siteName, @Param("serviceType") ServiceType serviceType);

    @Query("SELECT new tn.esprit.projetintegre.dto.PackQualityDTO(" +
           "  p.id, p.name, s.name, s.city, p.price, p.rating, " +
           "  COUNT(DISTINCT sr.id), " +
           "  AVG(CAST(sr.qualityRating AS double)), " +
           "  AVG(CAST(sr.valueRating AS double)), " +
           "  null, null) " +
           "FROM Pack p " +
           "LEFT JOIN p.site s " +
           "LEFT JOIN p.services cs " +
           "LEFT JOIN ServiceReview sr ON sr.service.id = cs.id AND sr.isApproved = true " +
           "WHERE p.isActive = true " +
           "GROUP BY p.id, p.name, s.id, s.name, s.city, p.price, p.rating " +
           "ORDER BY p.rating DESC")
    List<PackQualityDTO> findPackQualityMetrics();
}
