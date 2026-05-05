package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.TicketReservation;
import tn.esprit.projetintegre.enums.ReservationStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketReservationRepository extends JpaRepository<TicketReservation, Long> {

    @EntityGraph(attributePaths = {"user", "event"})
    Optional<TicketReservation> findByReservationCode(String code);

    @EntityGraph(attributePaths = {"user", "event"})
    List<TicketReservation> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "event"})
    Page<TicketReservation> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "event"})
    List<TicketReservation> findByEventId(Long eventId);

    @EntityGraph(attributePaths = {"user", "event"})
    List<TicketReservation> findByStatus(ReservationStatus status);

    @EntityGraph(attributePaths = {"user", "event"})
    List<TicketReservation> findByUserIdAndStatus(Long userId, ReservationStatus status);

    boolean existsByReservationCode(String code);

    @EntityGraph(attributePaths = {"user", "event"})
    Optional<TicketReservation> findById(Long id);
}
