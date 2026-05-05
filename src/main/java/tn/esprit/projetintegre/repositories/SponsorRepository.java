package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Sponsor;

import java.util.List;
import java.util.Optional;
@Repository
public interface SponsorRepository extends JpaRepository<Sponsor, Long> {

    Optional<Sponsor> findById(Long id);

    Optional<Sponsor> findByEmail(String email);

    List<Sponsor> findByIsActiveTrue();

    List<Sponsor> findByCity(String city);

    List<Sponsor> findByCountry(String country);

    @Query("SELECT s FROM Sponsor s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Sponsor> searchByKeyword(String keyword);

    boolean existsByEmail(String email);
}
