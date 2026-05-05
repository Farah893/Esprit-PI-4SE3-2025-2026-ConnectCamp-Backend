package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ReservationHistory;
import tn.esprit.projetintegre.enums.ReservationStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationHistoryRepository extends JpaRepository<ReservationHistory, Long> {

    @Override
    @EntityGraph(attributePaths = {"reservation", "changedBy"})
    Optional<ReservationHistory> findById(Long id);

    @EntityGraph(attributePaths = {"reservation", "changedBy"})
    List<ReservationHistory> findByReservationIdOrderByChangedAtDesc(Long reservationId);

    @EntityGraph(attributePaths = {"reservation", "changedBy"})
    List<ReservationHistory> findByStatus(ReservationStatus status);

    @EntityGraph(attributePaths = {"reservation", "changedBy"})
    List<ReservationHistory> findByChangedById(Long userId);
}
