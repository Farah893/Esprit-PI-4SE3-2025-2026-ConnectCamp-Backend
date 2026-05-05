package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.CertificationItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificationItemRepository extends JpaRepository<CertificationItem, Long> {

    @EntityGraph(attributePaths = {"certification"}) // Charge la relation avec la certification
    Optional<CertificationItem> findById(Long id);

    @EntityGraph(attributePaths = {"certification"})
    List<CertificationItem> findByCertificationId(Long certificationId);

    @EntityGraph(attributePaths = {"certification"})
    List<CertificationItem> findByCertificationIdAndPassed(Long certificationId, Boolean passed);

    List<CertificationItem> findByCertification_Id(Long certificationId);
}
