package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.UserMission;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMissionRepository extends JpaRepository<UserMission, Long> {

    @EntityGraph(attributePaths = {"user", "mission"})
    List<UserMission> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "mission"})
    List<UserMission> findByUserIdAndIsCompletedFalse(Long userId);

    @EntityGraph(attributePaths = {"user", "mission"})
    Optional<UserMission> findByUserIdAndMissionId(Long userId, Long missionId);
}
