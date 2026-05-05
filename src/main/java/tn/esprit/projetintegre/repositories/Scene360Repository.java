package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Scene360;

import java.util.List;
import java.util.Optional;

@Repository
public interface Scene360Repository extends JpaRepository<Scene360, Long> {

    @Override
    @EntityGraph(attributePaths = {"virtualTour"})
    Optional<Scene360> findById(Long id);

    @EntityGraph(attributePaths = {"virtualTour"})
    List<Scene360> findByVirtualTourIdOrderByOrderIndexAsc(Long virtualTourId);
}
