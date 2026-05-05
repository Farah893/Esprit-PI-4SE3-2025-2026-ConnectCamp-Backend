package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.EmergencyIntervention;
import tn.esprit.projetintegre.enums.EmergencyType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyInterventionRepository extends JpaRepository<EmergencyIntervention, Long> {

    @EntityGraph(attributePaths = {"alert", "site"}) // Charge les relations nécessaires
    Optional<EmergencyIntervention> findById(Long id);

    @EntityGraph(attributePaths = {"alert", "site"})
    Optional<EmergencyIntervention> findByInterventionCode(String interventionCode);

    @EntityGraph(attributePaths = {"alert", "site"})
    Page<EmergencyIntervention> findByStatus(String status, Pageable pageable);

    @EntityGraph(attributePaths = {"alert", "site"})
    Page<EmergencyIntervention> findByAlertId(Long alertId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"alert", "site"})
    List<EmergencyIntervention> findByAlertId(Long alertId);

    @EntityGraph(attributePaths = {"alert", "site"})
    Page<EmergencyIntervention> findBySiteId(Long siteId, Pageable pageable);

    @EntityGraph(attributePaths = {"alert", "site"})
    @Query("SELECT ei FROM EmergencyIntervention ei WHERE ei.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY ei.dispatchedAt DESC")
    List<EmergencyIntervention> findActiveInterventions();

    @EntityGraph(attributePaths = {"alert", "site"})
    @Query("SELECT ei FROM EmergencyIntervention ei WHERE ei.site.id = :siteId AND ei.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<EmergencyIntervention> findActiveInterventionsBySite(@Param("siteId") Long siteId);

    @EntityGraph(attributePaths = {"alert", "site"})
    @Query("SELECT ei FROM EmergencyIntervention ei WHERE ei.dispatchedAt >= :startDate AND ei.dispatchedAt <= :endDate")
    Page<EmergencyIntervention> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT AVG(ei.responseTimeMinutes) FROM EmergencyIntervention ei WHERE ei.site.id = :siteId AND ei.responseTimeMinutes IS NOT NULL")
    Double getAverageResponseTime(@Param("siteId") Long siteId);

    @Query("SELECT SUM(ei.evacuatedCount) FROM EmergencyIntervention ei WHERE ei.site.id = :siteId")
    Long getTotalEvacuated(@Param("siteId") Long siteId);
}
