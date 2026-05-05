package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.EmailNotification;
import tn.esprit.projetintegre.enums.EmailType;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"}) // Charge l'utilisateur destinataire
    Optional<EmailNotification> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    List<EmailNotification> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    Page<EmailNotification> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    List<EmailNotification> findByEmailType(EmailType type);

    @EntityGraph(attributePaths = {"user"})
    List<EmailNotification> findBySent(Boolean sent);

    @EntityGraph(attributePaths = {"user"})
    List<EmailNotification> findByToEmail(String email);

    @EntityGraph(attributePaths = {"user"})
    List<EmailNotification> findByOpened(Boolean opened);
}
