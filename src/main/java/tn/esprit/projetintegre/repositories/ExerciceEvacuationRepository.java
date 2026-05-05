package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ExerciceEvacuation;
import tn.esprit.projetintegre.enums.TypeExercice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciceEvacuationRepository extends JpaRepository<ExerciceEvacuation, Long> {

    @Override
    @EntityGraph(attributePaths = {"site", "responsable"}) // Charge les relations nécessaires
    Optional<ExerciceEvacuation> findById(Long id);

    @EntityGraph(attributePaths = {"site", "responsable"})
    List<ExerciceEvacuation> findBySiteId(Long siteId);

    @EntityGraph(attributePaths = {"site", "responsable"})
    Page<ExerciceEvacuation> findBySiteId(Long siteId, Pageable pageable);

    @EntityGraph(attributePaths = {"site", "responsable"})
    List<ExerciceEvacuation> findByTypeExercice(TypeExercice type);

    @EntityGraph(attributePaths = {"site", "responsable"})
    List<ExerciceEvacuation> findByTermine(Boolean termine);

    @EntityGraph(attributePaths = {"site", "responsable"})
    @Query("SELECT e FROM ExerciceEvacuation e WHERE e.datePlanifiee BETWEEN :start AND :end")
    List<ExerciceEvacuation> findByDateRange(LocalDateTime start, LocalDateTime end);
}
