package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ProtocoleUrgence;
import tn.esprit.projetintegre.enums.TypeUrgence;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProtocoleUrgenceRepository extends JpaRepository<ProtocoleUrgence, Long> {

    @Override
    @EntityGraph(attributePaths = {"site"}) // Charge le site associé au protocole
    Optional<ProtocoleUrgence> findById(Long id);

    @EntityGraph(attributePaths = {"site"})
    List<ProtocoleUrgence> findByTypeUrgence(TypeUrgence type);

    @EntityGraph(attributePaths = {"site"})
    List<ProtocoleUrgence> findBySiteId(Long siteId);

    @EntityGraph(attributePaths = {"site"})
    List<ProtocoleUrgence> findByActif(Boolean actif);

    @EntityGraph(attributePaths = {"site"})
    List<ProtocoleUrgence> findBySiteIdAndActif(Long siteId, Boolean actif);
}
