package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.dto.request.MessageRequest;
import tn.esprit.projetintegre.dto.response.MessageResponse;
import tn.esprit.projetintegre.entities.ChatRoom;
import tn.esprit.projetintegre.entities.Message;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.ChatRoomRepository;
import tn.esprit.projetintegre.repositories.MessageRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    // ── Send a message ──────────────────────────────────────────────────────────
    public MessageResponse sendMessage(MessageRequest request) {
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found with ID: " + request.getSenderId()));

        User receiver = null;
        if (request.getReceiverId() != null) {
            receiver = userRepository.findById(request.getReceiverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with ID: " + request.getReceiverId()));
        }

        ChatRoom chatRoom = null;
        if (request.getChatRoomId() != null) {
            chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("ChatRoom not found with ID: " + request.getChatRoomId()));
        }

        Message replyTo = null;
        if (request.getReplyToId() != null) {
            replyTo = messageRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Replied message not found with ID: " + request.getReplyToId()));
        }

        Message message = Message.builder()
                .content(request.getContent())
                .messageType(request.getMessageType())
                .mediaUrl(request.getMediaUrl())
                .fileName(request.getFileName())
                .fileSize(request.getFileSize())
                .sentAt(LocalDateTime.now())
                .sender(sender)
                .receiver(receiver)
                .chatRoom(chatRoom)
                .replyTo(replyTo)
                .isRead(false)
                .isEdited(false)
                .isDeleted(false)
                .build();

        message = messageRepository.save(message);
        return toResponse(message);
    }

    // ── Get message by ID ───────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public MessageResponse getMessageById(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + id));
        return toResponse(message);
    }

    // ── Get all messages in a chat room (ordered, paginated) ────────────────────
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessagesByChatRoom(Long chatRoomId, Pageable pageable) {
        return messageRepository.findByChatRoomIdOrderBySentAtDesc(chatRoomId, pageable)
                .map(this::toResponse);
    }

    // ── Get all messages in a chat room (ordered list) ──────────────────────────
    @Transactional(readOnly = true)
    public List<MessageResponse> getChatRoomMessages(Long chatRoomId) {
        return messageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Get private conversation between two users ──────────────────────────────
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversation(Long userId1, Long userId2) {
        return messageRepository.findConversation(userId1, userId2)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Edit a message ──────────────────────────────────────────────────────────
    public MessageResponse editMessage(Long messageId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        message.setContent(newContent);
        message.setIsEdited(true);
        message.setEditedAt(LocalDateTime.now());

        return toResponse(messageRepository.save(message));
    }

    // ── Soft delete a message ───────────────────────────────────────────────────
    public MessageResponse deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        message.setIsDeleted(true);
        message.setDeletedAt(LocalDateTime.now());
        message.setContent("[Message supprimé]");

        return toResponse(messageRepository.save(message));
    }

    // ── Mark messages as read ───────────────────────────────────────────────────
    public void markConversationAsRead(Long receiverId, Long senderId) {
        messageRepository.markAsRead(receiverId, senderId);
    }

    // ── Count unread messages ───────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public long countUnreadMessages(Long receiverId) {
        return messageRepository.countByReceiverIdAndIsRead(receiverId, false);
    }

    // ── Get unread messages ─────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<MessageResponse> getUnreadMessages(Long receiverId) {
        return messageRepository.findByReceiverIdAndIsRead(receiverId, false)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Mapper ──────────────────────────────────────────────────────────────────
    private MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .mediaUrl(message.getMediaUrl())
                .thumbnailUrl(message.getThumbnailUrl())
                .fileType(message.getFileType())
                .fileName(message.getFileName())
                .fileSize(message.getFileSize())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .isEdited(message.getIsEdited())
                .editedAt(message.getEditedAt())
                .isDeleted(message.getIsDeleted())
                .sentAt(message.getSentAt())
                .createdAt(message.getCreatedAt())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderName(message.getSender() != null ? message.getSender().getName() : null)
                .receiverId(message.getReceiver() != null ? message.getReceiver().getId() : null)
                .receiverName(message.getReceiver() != null ? message.getReceiver().getName() : null)
                .chatRoomId(message.getChatRoom() != null ? message.getChatRoom().getId() : null)
                .replyToId(message.getReplyTo() != null ? message.getReplyTo().getId() : null)
                .reactions(message.getReactions() != null
                        ? message.getReactions().stream()
                        .map(r -> tn.esprit.projetintegre.dto.response.MessageReactionResponse.builder()
                                .id(r.getId())
                                .emoji(r.getEmoji())
                                .userId(r.getUser() != null ? r.getUser().getId() : null)
                                .userName(r.getUser() != null ? r.getUser().getName() : null)
                                .createdAt(r.getCreatedAt())
                                .build())
                        .collect(Collectors.toList())
                        : null)
                .build();
    }
}
