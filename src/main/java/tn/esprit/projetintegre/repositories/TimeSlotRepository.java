package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.TimeSlot;
import tn.esprit.projetintegre.enums.AvailabilityStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    @EntityGraph(attributePaths = {"event", "service"})
    List<TimeSlot> findByEventId(Long eventId);

    @EntityGraph(attributePaths = {"event", "service"})
    List<TimeSlot> findByServiceId(Long serviceId);

    @EntityGraph(attributePaths = {"event", "service"})
    List<TimeSlot> findByDate(LocalDate date);

    @EntityGraph(attributePaths = {"event", "service"})
    List<TimeSlot> findByDateAndStatus(LocalDate date, AvailabilityStatus status);

    @EntityGraph(attributePaths = {"event", "service"})
    List<TimeSlot> findByEventIdAndDate(Long eventId, LocalDate date);

    @EntityGraph(attributePaths = {"event", "service"})
    List<TimeSlot> findByServiceIdAndDate(Long serviceId, LocalDate date);

    @EntityGraph(attributePaths = {"event", "service"})
    @Query("SELECT t FROM TimeSlot t WHERE t.event.id = :eventId AND t.date BETWEEN :startDate AND :endDate")
    List<TimeSlot> findByEventIdAndDateRange(Long eventId, LocalDate startDate, LocalDate endDate);

    @EntityGraph(attributePaths = {"event", "service"})
    @Query("SELECT t FROM TimeSlot t WHERE t.status = 'AVAILABLE' AND t.bookedCount < t.capacity")
    List<TimeSlot> findAvailableSlots();

    @EntityGraph(attributePaths = {"event", "service"})
    Optional<TimeSlot> findById(Long id);
}
