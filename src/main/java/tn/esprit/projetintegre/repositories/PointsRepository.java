package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Points;
import tn.esprit.projetintegre.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PointsRepository extends JpaRepository<Points, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"}) // Charge l'utilisateur propriétaire des points
    Optional<Points> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    List<Points> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    Page<Points> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    List<Points> findByTransactionType(TransactionType type);

    @EntityGraph(attributePaths = {"user"})
    List<Points> findByUserIdAndExpired(Long userId, Boolean expired);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT SUM(p.points) FROM Points p WHERE p.user.id = :userId AND p.expired = false AND p.used = false")
    Integer getAvailablePoints(Long userId);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM Points p WHERE p.expirationDate <= :date AND p.expired = false")
    List<Points> findExpiringPoints(LocalDateTime date);
}
