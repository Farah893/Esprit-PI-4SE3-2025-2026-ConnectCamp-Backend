package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.CandidatureService;
import tn.esprit.projetintegre.enums.StatutCandidature;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidatureServiceRepository extends JpaRepository<CandidatureService, Long> {

    @EntityGraph(attributePaths = { "candidat", "eventService" }) // Charge les relations nécessaires
    Optional<CandidatureService> findByNumeroCandidature(String numero);

    @EntityGraph(attributePaths = { "candidat", "eventService" })
    List<CandidatureService> findByCandidatId(Long userId);

    @EntityGraph(attributePaths = { "candidat", "eventService" })
    Page<CandidatureService> findByCandidatId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = { "candidat", "eventService" })
    List<CandidatureService> findByEventServiceId(Long eventServiceId);

    @EntityGraph(attributePaths = { "candidat", "eventService" })
    List<CandidatureService> findByStatut(StatutCandidature statut);

    @EntityGraph(attributePaths = { "candidat", "eventService" })
    List<CandidatureService> findByEventServiceIdAndStatut(Long eventServiceId, StatutCandidature statut);

    @EntityGraph(attributePaths = { "candidat", "eventService" })
    List<CandidatureService> findByEventServiceEventId(Long eventId);

    @EntityGraph(attributePaths = { "candidat", "eventService", "eventService.event" })
    List<CandidatureService> findByEventService_Event_Organizer_User_Id(Long userId);

    @EntityGraph(attributePaths = { "candidat", "eventService" })
    List<CandidatureService> findByEventService_Event_Organizer_Id(Long organizerId);

    boolean existsByCandidatIdAndEventServiceId(Long userId, Long eventServiceId);
}
