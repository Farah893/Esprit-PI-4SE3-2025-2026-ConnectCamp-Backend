package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Abonnement;
import tn.esprit.projetintegre.enums.StatutAbonnement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    @EntityGraph(attributePaths = {"user", "plan"}) // Charge l'utilisateur et le plan d'abonnement
    Optional<Abonnement> findByNumeroAbonnement(String numero);

    @EntityGraph(attributePaths = {"user", "plan"})
    List<Abonnement> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "plan"})
    Page<Abonnement> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "plan"})
    List<Abonnement> findByStatut(StatutAbonnement statut);

    @EntityGraph(attributePaths = {"user", "plan"})
    List<Abonnement> findByUserIdAndStatut(Long userId, StatutAbonnement statut);

    @EntityGraph(attributePaths = {"user", "plan"})
    @Query("SELECT a FROM Abonnement a WHERE a.prochainPaiement <= :date AND a.statut = 'ACTIF'")
    List<Abonnement> findDueForPayment(LocalDate date);

    @EntityGraph(attributePaths = {"user", "plan"})
    @Query("SELECT a FROM Abonnement a WHERE a.dateFin <= :date AND a.statut = 'ACTIF'")
    List<Abonnement> findExpiring(LocalDate date);

    @EntityGraph(attributePaths = {"user", "plan"})
    Optional<Abonnement> findById(Long id);
}
