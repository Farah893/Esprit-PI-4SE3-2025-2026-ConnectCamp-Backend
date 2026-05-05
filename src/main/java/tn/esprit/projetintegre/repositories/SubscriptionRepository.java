package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Subscription;
import tn.esprit.projetintegre.enums.SubscriptionStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Subscription> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Page<Subscription> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    List<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

    @EntityGraph(attributePaths = {"user"})
    List<Subscription> findByStatus(SubscriptionStatus status);
}
