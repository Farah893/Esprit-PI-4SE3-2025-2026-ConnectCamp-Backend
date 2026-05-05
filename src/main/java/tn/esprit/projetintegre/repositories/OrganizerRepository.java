package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Organizer;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Organizer> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<Organizer> findByUser_Id(Long userId);  // ← corrigé

    @EntityGraph(attributePaths = {"user"})
    List<Organizer> findByVerified(Boolean verified);

    @EntityGraph(attributePaths = {"user"})
    List<Organizer> findByActive(Boolean active);

    @EntityGraph(attributePaths = {"user"})
    Page<Organizer> findByActive(Boolean active, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT o FROM Organizer o WHERE LOWER(o.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Organizer> searchByCompanyName(String keyword);

    boolean existsByUser_Id(Long userId);  // ← corrigé
}
