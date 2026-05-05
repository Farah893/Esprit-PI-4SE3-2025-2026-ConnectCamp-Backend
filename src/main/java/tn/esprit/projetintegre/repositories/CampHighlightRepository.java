package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.projetintegre.entities.CampHighlight;
import tn.esprit.projetintegre.enums.HighlightCategory;

import java.util.List;
import java.util.Optional;

public interface CampHighlightRepository extends JpaRepository<CampHighlight, Long> {

    @EntityGraph(attributePaths = "site")
    @Query("SELECT h FROM CampHighlight h WHERE h.id = :id")
    Optional<CampHighlight> findByIdWithSite(@Param("id") Long id);

    @EntityGraph(attributePaths = "site")
    @Query("SELECT h FROM CampHighlight h")
    List<CampHighlight> findAllWithSite();

    @EntityGraph(attributePaths = "site")
    List<CampHighlight> findBySite_Id(Long siteId);

    @EntityGraph(attributePaths = "site")
    List<CampHighlight> findBySite_IdAndCategory(Long siteId, HighlightCategory category);
}
