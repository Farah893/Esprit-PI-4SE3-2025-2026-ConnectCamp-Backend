package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.SellerSettings;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerSettingsRepository extends JpaRepository<SellerSettings, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<SellerSettings> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<SellerSettings> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<SellerSettings> findByVacationMode(Boolean vacationMode);

    boolean existsByUserId(Long userId);
}
