package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ChatRoom;
import tn.esprit.projetintegre.enums.ChatRoomType;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @EntityGraph(attributePaths = {"creator", "members"})
    Page<ChatRoom> findByType(ChatRoomType type, Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "members"})
    Page<ChatRoom> findByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "members"})
    Page<ChatRoom> findByIsPublicTrueAndIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "members"})
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members m WHERE m.id = :userId")
    Page<ChatRoom> findByMemberId(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "members"})
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.creator.id = :userId")
    Page<ChatRoom> findByCreatorId(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "members"})
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.relatedEntityType = :entityType AND cr.relatedEntityId = :entityId")
    Optional<ChatRoom> findByRelatedEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    @EntityGraph(attributePaths = {"creator", "members"})
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isPublic = true AND cr.isActive = true AND (LOWER(cr.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(cr.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ChatRoom> searchPublicRooms(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "members"})
    @Query("SELECT cr FROM ChatRoom cr JOIN FETCH cr.members WHERE cr.id = :id")
    Optional<ChatRoom> findByIdWithMembers(@Param("id") Long id);

    @EntityGraph(attributePaths = {"creator", "members"})
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.type = :type AND cr.isActive = true ORDER BY cr.lastMessageAt DESC NULLS LAST")
    List<ChatRoom> findActiveRoomsByTypeOrderedByActivity(@Param("type") ChatRoomType type, Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "members"})
    Optional<ChatRoom> findById(Long id);
}
