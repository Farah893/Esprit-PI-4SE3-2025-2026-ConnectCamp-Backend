package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.TestPackage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestPackageRepository extends JpaRepository<TestPackage, Long> {

    @EntityGraph(attributePaths = {"service"})
    List<TestPackage> findByServiceId(Long serviceId);

    @EntityGraph(attributePaths = {"service"})
    List<TestPackage> findByIsActive(Boolean isActive);

    @EntityGraph(attributePaths = {"service"})
    @Query("SELECT t FROM TestPackage t WHERE t.isActive = true AND (t.validFrom IS NULL OR t.validFrom <= :date) AND (t.validUntil IS NULL OR t.validUntil >= :date)")
    List<TestPackage> findValidPackages(LocalDate date);

    @EntityGraph(attributePaths = {"service"})
    Optional<TestPackage> findById(Long id);
}
