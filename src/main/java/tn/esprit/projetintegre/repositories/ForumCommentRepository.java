package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ForumComment;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {

    @Override
    @EntityGraph(attributePaths = {"author", "article"})
    Optional<ForumComment> findById(Long id);

    @EntityGraph(attributePaths = {"author", "article"})
    List<ForumComment> findByArticleId(Long articleId);

    @EntityGraph(attributePaths = {"author", "article"})
    List<ForumComment> findByArticleIdAndParentIsNull(Long articleId);

    @EntityGraph(attributePaths = {"author", "article"})
    List<ForumComment> findByAuthorId(Long authorId);

    @EntityGraph(attributePaths = {"author", "article"})
    List<ForumComment> findByParentId(Long parentId);

    @EntityGraph(attributePaths = {"author", "article"})
    List<ForumComment> findByIsApprovedTrue();

    Integer countByArticleId(Long articleId);
}
