package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Rental;
import tn.esprit.projetintegre.enums.RentalStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "site"}) // Charge les relations nécessaires
    Optional<Rental> findById(Long id);

    @EntityGraph(attributePaths = {"user", "site"})
    Optional<Rental> findByRentalNumber(String number);

    @EntityGraph(attributePaths = {"user", "site"})
    List<Rental> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "site"})
    Page<Rental> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "site"})
    List<Rental> findByStatus(RentalStatus status);

    @EntityGraph(attributePaths = {"user", "site"})
    List<Rental> findBySiteId(Long siteId);

    @EntityGraph(attributePaths = {"user", "site"})
    @Query("SELECT r FROM Rental r WHERE r.status = 'ACTIVE' AND r.endDate < :date")
    List<Rental> findOverdueRentals(LocalDate date);

    @EntityGraph(attributePaths = {"user", "site"})
    @Query("SELECT r FROM Rental r WHERE r.startDate BETWEEN :start AND :end")
    List<Rental> findByDateRange(LocalDate start, LocalDate end);
}
