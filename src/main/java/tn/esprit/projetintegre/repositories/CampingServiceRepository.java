package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.CampingService;
import tn.esprit.projetintegre.enums.ServiceType;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampingServiceRepository extends JpaRepository<CampingService, Long> {

    @Override
    @EntityGraph(attributePaths = { "site", "provider" })
    Optional<CampingService> findById(Long id);

    @EntityGraph(attributePaths = { "site", "provider" })
    Page<CampingService> findByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = { "site", "provider" })
    List<CampingService> findByType(ServiceType type);

    @EntityGraph(attributePaths = { "site", "provider" })
    Page<CampingService> findBySiteId(Long siteId, Pageable pageable);

    @EntityGraph(attributePaths = { "site", "provider" })
    Page<CampingService> findByProviderId(Long providerId, Pageable pageable);

    @EntityGraph(attributePaths = { "site", "provider" })
    Page<CampingService> findByIsActiveTrueAndIsOrganizerServiceTrue(Pageable pageable);

    @Query("SELECT s.name, AVG(r.rating), COUNT(DISTINCT c.id) " +
           "FROM CampingService s " +
           "LEFT JOIN ServiceReview r ON r.service.id = s.id " +
           "LEFT JOIN CandidatureService c ON c.service.id = s.id " +
           "GROUP BY s.id, s.name " +
           "HAVING COUNT(DISTINCT c.id) > 2 AND AVG(r.rating) < 3.0")
    List<Object[]> findAtRiskServices();
}