package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.EvacuationExercise;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EvacuationExerciseRepository extends JpaRepository<EvacuationExercise, Long> {

    @EntityGraph(attributePaths = {"site", "supervisor"}) // Charge les relations nécessaires
    Optional<EvacuationExercise> findById(Long id);

    @EntityGraph(attributePaths = {"site", "supervisor"})
    Optional<EvacuationExercise> findByExerciseCode(String exerciseCode);

    @EntityGraph(attributePaths = {"site", "supervisor"})
    Page<EvacuationExercise> findBySiteId(Long siteId, Pageable pageable);

    @EntityGraph(attributePaths = {"site", "supervisor"})
    Page<EvacuationExercise> findByIsCompletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = {"site", "supervisor"})
    @Query("SELECT ee FROM EvacuationExercise ee WHERE ee.scheduledDate > :now AND ee.isCompleted = false ORDER BY ee.scheduledDate ASC")
    List<EvacuationExercise> findUpcomingExercises(@Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"site", "supervisor"})
    @Query("SELECT ee FROM EvacuationExercise ee WHERE ee.site.id = :siteId AND ee.scheduledDate > :now AND ee.isCompleted = false")
    List<EvacuationExercise> findUpcomingExercisesBySite(@Param("siteId") Long siteId, @Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"site", "supervisor"})
    @Query("SELECT ee FROM EvacuationExercise ee WHERE ee.site.id = :siteId AND ee.isCompleted = true ORDER BY ee.completedAt DESC")
    Page<EvacuationExercise> findCompletedExercisesBySite(@Param("siteId") Long siteId, Pageable pageable);

    @EntityGraph(attributePaths = {"site", "supervisor"})
    @Query("SELECT AVG(ee.evacuationTimeSeconds) FROM EvacuationExercise ee WHERE ee.site.id = :siteId AND ee.isCompleted = true AND ee.evacuationTimeSeconds IS NOT NULL")
    Double getAverageEvacuationTime(@Param("siteId") Long siteId);
}
