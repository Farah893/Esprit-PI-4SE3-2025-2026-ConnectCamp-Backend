package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Ticket;
import tn.esprit.projetintegre.enums.TicketStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @EntityGraph(attributePaths = {"event", "user"})
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    @EntityGraph(attributePaths = {"event", "user"})
    Page<Ticket> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"event", "user"})
    Page<Ticket> findByEventId(Long eventId, Pageable pageable);

    @EntityGraph(attributePaths = {"event", "user"})
    List<Ticket> findByEventIdAndStatus(Long eventId, TicketStatus status);

    @EntityGraph(attributePaths = {"event", "user"})
    long countByEventIdAndStatus(Long eventId, TicketStatus status);

    @EntityGraph(attributePaths = {"event", "user"})
    Optional<Ticket> findById(Long id);
}
