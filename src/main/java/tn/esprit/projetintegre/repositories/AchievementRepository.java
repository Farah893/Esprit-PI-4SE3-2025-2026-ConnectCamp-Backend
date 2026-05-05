package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Achievement;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    @Override
    @EntityGraph(attributePaths = {"userAchievements"}) // Charge la relation avec les utilisateurs si elle existe
    Optional<Achievement> findById(Long id);

    @EntityGraph(attributePaths = {"userAchievements"})
    List<Achievement> findByIsActiveTrue();

    @EntityGraph(attributePaths = {"userAchievements"})
    List<Achievement> findByCategory(String category);
}
