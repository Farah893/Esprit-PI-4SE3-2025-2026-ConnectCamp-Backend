package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Mission;
import tn.esprit.projetintegre.enums.MissionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

    @Override
    @EntityGraph(attributePaths = {"createdBy"}) // Charge les relations nécessaires
    Optional<Mission> findById(Long id);

    @EntityGraph(attributePaths = {"createdBy"})
    List<Mission> findByIsActiveTrue();

    @EntityGraph(attributePaths = {"createdBy"})
    List<Mission> findByType(MissionType type);

    @EntityGraph(attributePaths = {"createdBy"})
    @Query("SELECT m FROM Mission m WHERE m.isActive = true AND (m.startDate IS NULL OR m.startDate <= :now) AND (m.endDate IS NULL OR m.endDate >= :now)")
    List<Mission> findActiveMissions(LocalDateTime now);
}
