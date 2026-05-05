package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.LoyaltyProgram;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyProgramRepository extends JpaRepository<LoyaltyProgram, Long> {

    @Override
    @EntityGraph(attributePaths = {}) // À remplir si vous ajoutez des relations @ManyToOne ou @OneToMany plus tard
    Optional<LoyaltyProgram> findById(Long id);

    @EntityGraph(attributePaths = {})
    Optional<LoyaltyProgram> findByName(String name);

    @EntityGraph(attributePaths = {})
    List<LoyaltyProgram> findByIsActive(Boolean isActive);

    boolean existsByName(String name);
}
