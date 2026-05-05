package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.AlerteUrgence;
import tn.esprit.projetintegre.enums.StatutAlerte;
import tn.esprit.projetintegre.enums.TypeUrgence;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlerteUrgenceRepository extends JpaRepository<AlerteUrgence, Long> {

    @Override
    @EntityGraph(attributePaths = {"site", "signalePar"}) // Charge les relations nécessaires
    Optional<AlerteUrgence> findById(Long id);

    @EntityGraph(attributePaths = {"site", "signalePar"})
    List<AlerteUrgence> findByStatut(StatutAlerte statut);

    @EntityGraph(attributePaths = {"site", "signalePar"})
    Page<AlerteUrgence> findByStatut(StatutAlerte statut, Pageable pageable);

    @EntityGraph(attributePaths = {"site", "signalePar"})
    List<AlerteUrgence> findByTypeUrgence(TypeUrgence type);

    @EntityGraph(attributePaths = {"site", "signalePar"})
    List<AlerteUrgence> findBySiteId(Long siteId);

    @EntityGraph(attributePaths = {"site", "signalePar"})
    List<AlerteUrgence> findBySiteIdAndStatut(Long siteId, StatutAlerte statut);

    @EntityGraph(attributePaths = {"site", "signalePar"})
    List<AlerteUrgence> findBySignaleParId(Long userId);
}
