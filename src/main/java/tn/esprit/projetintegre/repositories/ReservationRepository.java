package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Reservation;
import tn.esprit.projetintegre.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Override
    @EntityGraph(attributePaths = { "user", "site" })
    Optional<Reservation> findById(Long id);

    @EntityGraph(attributePaths = { "user", "site" })
    Optional<Reservation> findByReservationNumber(String reservationNumber);

    @EntityGraph(attributePaths = { "user", "site" })
    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = { "user", "site" })
    Page<Reservation> findBySiteId(Long siteId, Pageable pageable);

    @EntityGraph(attributePaths = { "user", "site" })
    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = { "user", "site" })
    @Query("SELECT r FROM Reservation r WHERE r.site.id = :siteId AND r.status = 'CONFIRMED' AND ((r.checkInDate BETWEEN :startDate AND :endDate) OR (r.checkOutDate BETWEEN :startDate AND :endDate))")
    List<Reservation> findOverlappingReservations(Long siteId, LocalDateTime startDate, LocalDateTime endDate);

    @EntityGraph(attributePaths = { "user", "event" })
    List<Reservation> findByEventIdAndStatus(Long eventId, ReservationStatus status);
}
