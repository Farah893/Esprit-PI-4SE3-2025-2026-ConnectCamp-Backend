package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Message;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Override
    @EntityGraph(attributePaths = {"sender", "receiver", "chatRoom"}) // Charge les relations nécessaires
    Optional<Message> findById(Long id);

    @EntityGraph(attributePaths = {"sender", "receiver", "chatRoom"})
    List<Message> findBySenderId(Long senderId);

    @EntityGraph(attributePaths = {"sender", "receiver", "chatRoom"})
    List<Message> findByReceiverId(Long receiverId);

    @EntityGraph(attributePaths = {"sender", "receiver", "chatRoom"})
    List<Message> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);

    @EntityGraph(attributePaths = {"sender", "receiver", "chatRoom"})
    Page<Message> findByChatRoomIdOrderBySentAtDesc(Long chatRoomId, Pageable pageable);

    @EntityGraph(attributePaths = {"sender", "receiver", "chatRoom"})
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2) OR (m.sender.id = :userId2 AND m.receiver.id = :userId1) ORDER BY m.sentAt ASC")
    List<Message> findConversation(Long userId1, Long userId2);

    @EntityGraph(attributePaths = {"sender", "receiver", "chatRoom"})
    List<Message> findByReceiverIdAndIsRead(Long receiverId, Boolean isRead);

    @EntityGraph(attributePaths = {"sender", "receiver", "chatRoom"})
    long countByReceiverIdAndIsRead(Long receiverId, Boolean isRead);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId")
    void markAsRead(Long receiverId, Long senderId);
}
