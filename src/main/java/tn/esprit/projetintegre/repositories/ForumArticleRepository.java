package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ForumArticle;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForumArticleRepository extends JpaRepository<ForumArticle, Long> {

    @Override
    @EntityGraph(attributePaths = {"author", "category"}) // Charge les relations nécessaires
    Optional<ForumArticle> findById(Long id);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<ForumArticle> findByIsPublishedTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<ForumArticle> findByAuthorId(Long authorId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<ForumArticle> findByCategory(String category, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    List<ForumArticle> findByIsPinnedTrue();

    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT a FROM ForumArticle a WHERE a.isPublished = true AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ForumArticle> searchArticles(String keyword, Pageable pageable);
}
