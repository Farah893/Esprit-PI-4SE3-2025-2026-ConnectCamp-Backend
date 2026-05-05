package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.TicketRequest;
import tn.esprit.projetintegre.enums.TicketRequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRequestRepository extends JpaRepository<TicketRequest, Long> {

    @EntityGraph(attributePaths = {"user", "event"})
    Optional<TicketRequest> findByRequestNumber(String requestNumber);

    @EntityGraph(attributePaths = {"user", "event"})
    Page<TicketRequest> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "event"})
    Page<TicketRequest> findByEventId(Long eventId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "event"})
    Page<TicketRequest> findByStatus(TicketRequestStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "event"})
    List<TicketRequest> findByUserIdAndEventId(Long userId, Long eventId);

    @EntityGraph(attributePaths = {"user", "event"})
    @Query("SELECT tr FROM TicketRequest tr WHERE tr.user.id = :userId AND tr.status = :status")
    Page<TicketRequest> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TicketRequestStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "event"})
    @Query("SELECT tr FROM TicketRequest tr WHERE tr.event.id = :eventId AND tr.status = :status")
    Page<TicketRequest> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") TicketRequestStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "event"})
    @Query("SELECT tr FROM TicketRequest tr WHERE tr.status = 'PENDING' AND tr.expiresAt < :now")
    List<TicketRequest> findExpiredRequests(@Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"user", "event"})
    @Query("SELECT COUNT(tr) FROM TicketRequest tr WHERE tr.event.id = :eventId AND tr.status = :status")
    Long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") TicketRequestStatus status);

    @EntityGraph(attributePaths = {"user", "event"})
    @Query("SELECT SUM(tr.quantity) FROM TicketRequest tr WHERE tr.event.id = :eventId AND tr.status = 'APPROVED'")
    Integer getTotalApprovedTicketsForEvent(@Param("eventId") Long eventId);

    @EntityGraph(attributePaths = {"user", "event"})
    @Query("SELECT tr FROM TicketRequest tr JOIN FETCH tr.user JOIN FETCH tr.event WHERE tr.id = :id")
    Optional<TicketRequest> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "event"})
    Optional<TicketRequest> findById(Long id);
}
