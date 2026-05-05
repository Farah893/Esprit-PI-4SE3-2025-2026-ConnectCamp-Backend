package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Participant;
import tn.esprit.projetintegre.enums.TicketStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Override
    @EntityGraph(attributePaths = {"event", "user", "ticket"}) // Charge les relations nécessaires
    Optional<Participant> findById(Long id);

    @EntityGraph(attributePaths = {"event", "user", "ticket"})
    List<Participant> findByEventId(Long eventId);

    @EntityGraph(attributePaths = {"event", "user", "ticket"})
    Page<Participant> findByEventId(Long eventId, Pageable pageable);

    @EntityGraph(attributePaths = {"event", "user", "ticket"})
    List<Participant> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"event", "user", "ticket"})
    Optional<Participant> findByEventIdAndUserId(Long eventId, Long userId);

    @EntityGraph(attributePaths = {"event", "user", "ticket"})
    Optional<Participant> findByTicketId(Long ticketId);

    @EntityGraph(attributePaths = {"event", "user", "ticket"})
    List<Participant> findByEventIdAndStatus(Long eventId, TicketStatus status);

    @EntityGraph(attributePaths = {"event", "user", "ticket"})
    @Query("SELECT p FROM Participant p WHERE p.event.id = :eventId AND p.checkedIn = :checkedIn")
    List<Participant> findByEventIdAndCheckedIn(@Param("eventId") Long eventId, @Param("checkedIn") Boolean checkedIn);

    @EntityGraph(attributePaths = {"event", "user", "ticket"})
    long countByEventId(Long eventId);

    @EntityGraph(attributePaths = {"event", "user", "ticket"})
    @Query("SELECT COUNT(p) FROM Participant p WHERE p.event.id = :eventId AND p.checkedIn = :checkedIn")
    long countByEventIdAndCheckedIn(@Param("eventId") Long eventId, @Param("checkedIn") Boolean checkedIn);
}
