package tn.esprit.projetintegre.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.request.MessageRequest;
import tn.esprit.projetintegre.dto.response.MessageResponse;
import tn.esprit.projetintegre.services.MessageService;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // ── Send a message (REST fallback — prefer WebSocket for real-time) ─────────
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(@Valid @RequestBody MessageRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Message envoyé avec succès", messageService.sendMessage(request)));
    }

    // ── Get a single message ────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> getMessageById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessageById(id)));
    }

    // ── Get chat room messages (paginated, newest first) ────────────────────────
    @GetMapping("/room/{chatRoomId}")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessagesByChatRoom(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessagesByChatRoom(chatRoomId, pageable)));
    }

    // ── Get full chat room history (ordered list) ───────────────────────────────
    @GetMapping("/room/{chatRoomId}/history")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getChatRoomHistory(@PathVariable Long chatRoomId) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getChatRoomMessages(chatRoomId)));
    }

    // ── Get private conversation between two users ──────────────────────────────
    @GetMapping("/conversation")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getConversation(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getConversation(userId1, userId2)));
    }

    // ── Edit a message ──────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> editMessage(
            @PathVariable Long id,
            @RequestParam String content) {
        return ResponseEntity.ok(ApiResponse.success("Message modifié avec succès", messageService.editMessage(id, content)));
    }

    // ── Delete a message (soft delete) ─────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteMessage(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Message supprimé avec succès", messageService.deleteMessage(id)));
    }

    // ── Mark conversation as read ───────────────────────────────────────────────
    @PutMapping("/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @RequestParam Long receiverId,
            @RequestParam Long senderId) {
        messageService.markConversationAsRead(receiverId, senderId);
        return ResponseEntity.ok(ApiResponse.success("Messages marqués comme lus", null));
    }
    
}
