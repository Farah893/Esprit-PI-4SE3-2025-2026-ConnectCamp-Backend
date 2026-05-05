package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.EventServiceEntity;
import tn.esprit.projetintegre.enums.ServiceType;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventServiceEntityRepository extends JpaRepository<EventServiceEntity, Long> {

    @Override
    @EntityGraph(attributePaths = { "event", "provider", "service" }) // Charge les relations nécessaires
    Optional<EventServiceEntity> findById(Long id);

    @EntityGraph(attributePaths = { "event", "provider", "service" })
    List<EventServiceEntity> findByEventId(Long eventId);

    @EntityGraph(attributePaths = { "event", "provider", "service" })
    List<EventServiceEntity> findByServiceType(ServiceType type);

    @EntityGraph(attributePaths = { "event", "provider", "service" })
    List<EventServiceEntity> findByEventIdAndIncluded(Long eventId, Boolean included);

    @EntityGraph(attributePaths = { "event", "provider", "service" })
    List<EventServiceEntity> findByProviderId(Long userId);

    @EntityGraph(attributePaths = { "event", "provider", "service" })
    List<EventServiceEntity> findByServiceId(Long serviceId);

    // --- Added for Module 1 Requirements --- //
    boolean existsByEventIdAndServiceId(Long eventId, Long serviceId);

    boolean existsByServiceId(Long serviceId);
    // --------------------------------------- //
}
