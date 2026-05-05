package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Facture;
import tn.esprit.projetintegre.enums.StatutTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "order", "abonnement"}) // Charge les relations nécessaires
    Optional<Facture> findById(Long id);

    @EntityGraph(attributePaths = {"user", "order", "abonnement"})
    Optional<Facture> findByNumeroFacture(String numeroFacture);

    @EntityGraph(attributePaths = {"user", "order", "abonnement"})
    Page<Facture> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "order", "abonnement"})
    Page<Facture> findByStatut(StatutTransaction statut, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "order", "abonnement"})
    List<Facture> findByDateEmissionBetween(LocalDate startDate, LocalDate endDate);

    @EntityGraph(attributePaths = {"user", "order", "abonnement"})
    Page<Facture> findByDateEmissionBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "order", "abonnement"})
    @Query("SELECT f FROM Facture f WHERE f.user.id = :userId AND f.statut = :statut")
    Page<Facture> findByUserIdAndStatut(@Param("userId") Long userId, @Param("statut") StatutTransaction statut, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "order", "abonnement"})
    @Query("SELECT SUM(f.montantTTC) FROM Facture f WHERE f.statut = 'PAYE'")
    BigDecimal getTotalRevenue();

    @EntityGraph(attributePaths = {"user", "order", "abonnement"})
    @Query("SELECT SUM(f.montantPaye) FROM Facture f WHERE f.datePaiement BETWEEN :startDate AND :endDate")
    BigDecimal getRevenueByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
