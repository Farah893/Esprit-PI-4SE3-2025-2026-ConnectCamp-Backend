package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Campsite;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampsiteRepository extends JpaRepository<Campsite, Long> {

    @EntityGraph(attributePaths = { "site" }) // Charge la relation avec le site
    @Override
    Optional<Campsite> findById(@org.springframework.lang.NonNull Long id);

    @EntityGraph(attributePaths = { "site" })
    List<Campsite> findBySiteId(Long siteId);

    @EntityGraph(attributePaths = { "site" })
    Page<Campsite> findBySiteId(Long siteId, Pageable pageable);

    @EntityGraph(attributePaths = { "site" })
    List<Campsite> findByType(String type);

    @EntityGraph(attributePaths = { "site" })
    List<Campsite> findByIsAvailable(Boolean isAvailable);

    @EntityGraph(attributePaths = { "site" })
    List<Campsite> findByIsActive(Boolean isActive);

    @EntityGraph(attributePaths = { "site" })
    List<Campsite> findBySiteIdAndIsAvailable(Long siteId, Boolean isAvailable);

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT c FROM Campsite c WHERE c.isActive = true AND c.pricePerNight BETWEEN :minPrice AND :maxPrice")
    List<Campsite> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT c FROM Campsite c WHERE c.isActive = true AND c.capacity >= :minCapacity")
    List<Campsite> findByMinCapacity(Integer minCapacity);
}
