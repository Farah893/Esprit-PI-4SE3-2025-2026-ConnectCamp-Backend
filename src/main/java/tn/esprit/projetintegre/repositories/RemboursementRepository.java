package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Remboursement;
import tn.esprit.projetintegre.enums.StatutRemboursement;

import java.util.List;
import java.util.Optional;

@Repository
public interface RemboursementRepository extends JpaRepository<Remboursement, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "order"})
    Optional<Remboursement> findById(Long id);

    @EntityGraph(attributePaths = {"user", "order"})
    Optional<Remboursement> findByNumeroRemboursement(String numero);

    @EntityGraph(attributePaths = {"user", "order"})
    List<Remboursement> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "order"})
    Page<Remboursement> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "order"})
    List<Remboursement> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"user", "order"})
    List<Remboursement> findByStatut(StatutRemboursement statut);

    @EntityGraph(attributePaths = {"user", "order"})
    Page<Remboursement> findByStatut(StatutRemboursement statut, Pageable pageable);
}
