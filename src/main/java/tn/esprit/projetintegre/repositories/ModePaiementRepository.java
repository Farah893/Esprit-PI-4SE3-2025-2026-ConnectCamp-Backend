package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ModePaiement;
import tn.esprit.projetintegre.enums.PaymentMethod;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModePaiementRepository extends JpaRepository<ModePaiement, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"}) // Charge l'utilisateur propriétaire du mode de paiement
    Optional<ModePaiement> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    List<ModePaiement> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<ModePaiement> findByUserIdAndActif(Long userId, Boolean actif);

    @EntityGraph(attributePaths = {"user"})
    Optional<ModePaiement> findByUserIdAndParDefaut(Long userId, Boolean parDefaut);

    @EntityGraph(attributePaths = {"user"})
    List<ModePaiement> findByType(PaymentMethod type);
}
