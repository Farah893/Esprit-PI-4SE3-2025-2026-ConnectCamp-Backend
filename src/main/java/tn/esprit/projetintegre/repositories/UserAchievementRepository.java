package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.UserAchievement;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    @EntityGraph(attributePaths = {"user", "achievement"})
    Page<UserAchievement> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "achievement"})
    List<UserAchievement> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "achievement"})
    List<UserAchievement> findByUserIdAndIsDisplayedTrue(Long userId);

    @EntityGraph(attributePaths = {"user", "achievement"})
    Optional<UserAchievement> findByUserIdAndAchievementId(Long userId, Long achievementId);

    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"user", "achievement"})
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user.id = :userId ORDER BY ua.unlockedAt DESC")
    List<UserAchievement> findRecentAchievements(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "achievement"})
    @Query("SELECT ua FROM UserAchievement ua JOIN ua.achievement a WHERE ua.user.id = :userId AND a.category = :category")
    List<UserAchievement> findByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);
}
