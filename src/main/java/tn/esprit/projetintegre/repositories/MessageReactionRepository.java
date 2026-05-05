package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.MessageReaction;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    @Override
    @EntityGraph(attributePaths = {"message", "user"}) // Charge les relations nécessaires
    Optional<MessageReaction> findById(Long id);

    @EntityGraph(attributePaths = {"message", "user"})
    List<MessageReaction> findByMessageId(Long messageId);

    @EntityGraph(attributePaths = {"message", "user"})
    List<MessageReaction> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"message", "user"})
    Optional<MessageReaction> findByMessageIdAndUserId(Long messageId, Long userId);

    @EntityGraph(attributePaths = {"message", "user"})
    void deleteByMessageIdAndUserId(Long messageId, Long userId);

    @EntityGraph(attributePaths = {"message", "user"})
    long countByMessageId(Long messageId);
}
