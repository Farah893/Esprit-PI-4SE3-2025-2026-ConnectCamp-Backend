package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Certification;
import tn.esprit.projetintegre.enums.CertificationStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    @EntityGraph(attributePaths = {"user"}) // Charge la relation avec l'utilisateur
    Optional<Certification> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<Certification> findByCertificationCode(String code);

    @EntityGraph(attributePaths = {"user"})
    List<Certification> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "site"})
    List<Certification> findBySiteId(Long siteId);

    @EntityGraph(attributePaths = {"user"})
    Page<Certification> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    List<Certification> findByStatus(CertificationStatus status);

    @EntityGraph(attributePaths = {"user"})
    List<Certification> findByUserIdAndStatus(Long userId, CertificationStatus status);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT c FROM Certification c WHERE c.user.id = :userId AND c.status = 'VALID'")
    List<Certification> findValidCertificationsByUserId(Long userId);

    boolean existsByCertificationCode(String code);
}
