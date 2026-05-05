package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.EventScheduleItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventScheduleItemRepository extends JpaRepository<EventScheduleItem, Long> {

    @Override
    @EntityGraph(attributePaths = {"event"}) // Charge la relation avec l'événement
    Optional<EventScheduleItem> findById(Long id);

    @EntityGraph(attributePaths = {"event"})
    List<EventScheduleItem> findByEventIdOrderByOrderIndexAsc(Long eventId);

    @EntityGraph(attributePaths = {"event"})
    List<EventScheduleItem> findByEventIdOrderByStartTimeAsc(Long eventId);

    @EntityGraph(attributePaths = {"event"})
    List<EventScheduleItem> findByType(String type);

    @EntityGraph(attributePaths = {"event"})
    List<EventScheduleItem> findByIsHighlight(Boolean isHighlight);

    @EntityGraph(attributePaths = {"event"})
    List<EventScheduleItem> findByIsBreak(Boolean isBreak);
}
