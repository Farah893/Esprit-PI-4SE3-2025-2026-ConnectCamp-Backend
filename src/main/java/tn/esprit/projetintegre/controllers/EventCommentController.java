package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.request.EventCommentRequest;
import tn.esprit.projetintegre.dto.response.EventCommentResponse;
import tn.esprit.projetintegre.entities.EventComment;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.EventCommentService;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://lively-wave-019e62b03.7.azurestaticapps.net"}, allowCredentials = "true")
@Tag(name = "Event Comments", description = "Event comment management APIs")
public class EventCommentController {

    private final EventCommentService eventCommentService;
    private final DtoMapper dtoMapper;

    @GetMapping
    @Operation(summary = "Get all event comments")
    public ResponseEntity<ApiResponse<List<EventCommentResponse>>> getAllComments() {
        List<EventComment> comments = eventCommentService.getAllComments();
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toEventCommentResponseList(comments)));
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get comments for a specific event")
    public ResponseEntity<ApiResponse<List<EventCommentResponse>>> getCommentsByEvent(@PathVariable Long eventId) {
        List<EventComment> comments = eventCommentService.getCommentsByEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toEventCommentResponseList(comments)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get comment by ID")
    public ResponseEntity<ApiResponse<EventCommentResponse>> getCommentById(@PathVariable Long id) {
        EventComment comment = eventCommentService.getCommentById(id);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toEventCommentResponse(comment)));
    }

    @PostMapping
    @Operation(summary = "Create a new event comment")
    public ResponseEntity<ApiResponse<EventCommentResponse>> createComment(
            @Valid @RequestBody EventCommentRequest request) {
        EventComment comment = eventCommentService.createComment(
                request.getEventId(),
                request.getUserId(),
                request.getContent());
        return ResponseEntity
                .ok(ApiResponse.success("Comment created successfully", dtoMapper.toEventCommentResponse(comment)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an event comment")
    public ResponseEntity<ApiResponse<EventCommentResponse>> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody String content) {
        EventComment updated = eventCommentService.updateComment(id, content);
        return ResponseEntity
                .ok(ApiResponse.success("Comment updated successfully", dtoMapper.toEventCommentResponse(updated)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an event comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        eventCommentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }
}
