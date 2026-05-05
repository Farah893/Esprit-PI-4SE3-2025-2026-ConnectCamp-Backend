package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.EmergencyProtocol;
import tn.esprit.projetintegre.enums.EmergencyType;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyProtocolRepository extends JpaRepository<EmergencyProtocol, Long> {

    @EntityGraph(attributePaths = {"site"}) // Charge la relation avec le site
    Optional<EmergencyProtocol> findById(Long id);

    @EntityGraph(attributePaths = {"site"})
    Optional<EmergencyProtocol> findByProtocolCode(String protocolCode);

    @EntityGraph(attributePaths = {"site"})
    Page<EmergencyProtocol> findByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"site"})
    Page<EmergencyProtocol> findByEmergencyType(EmergencyType type, Pageable pageable);

    @EntityGraph(attributePaths = {"site"})
    Page<EmergencyProtocol> findBySiteId(Long siteId, Pageable pageable);

    @EntityGraph(attributePaths = {"site"})
    @Query("SELECT ep FROM EmergencyProtocol ep WHERE ep.site.id = :siteId AND ep.isActive = true")
    List<EmergencyProtocol> findActiveProtocolsBySite(@Param("siteId") Long siteId);

    @EntityGraph(attributePaths = {"site"})
    @Query("SELECT ep FROM EmergencyProtocol ep WHERE ep.site.id = :siteId AND ep.emergencyType = :type AND ep.isActive = true")
    Optional<EmergencyProtocol> findBySiteIdAndType(@Param("siteId") Long siteId, @Param("type") EmergencyType type);

    @EntityGraph(attributePaths = {"site"})
    @Query("SELECT ep FROM EmergencyProtocol ep WHERE ep.requiresTraining = true AND ep.isActive = true")
    List<EmergencyProtocol> findProtocolsRequiringTraining();

    @EntityGraph(attributePaths = {"site"})
    @Query("SELECT ep FROM EmergencyProtocol ep WHERE ep.nextReviewDate IS NOT NULL AND ep.nextReviewDate <= CURRENT_DATE")
    List<EmergencyProtocol> findProtocolsNeedingReview();
}
