package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.PageResponse;
import tn.esprit.projetintegre.dto.request.EventRequest;
import tn.esprit.projetintegre.dto.response.EventResponse;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.enums.EventStatus;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/events")

@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://lively-wave-019e62b03.7.azurestaticapps.net"}, allowCredentials = "true")
@Tag(name = "Events", description = "Event management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class EventController {

    private final EventService eventService;
    private final DtoMapper dtoMapper;

    @GetMapping
    @Operation(summary = "Get all events")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toEventResponseList(events)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get events by status")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getEventsByStatus(
            @PathVariable EventStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Event> events = eventService.getEventsByStatus(status, PageRequest.of(page, size));
        Page<EventResponse> response = events.map(dtoMapper::toEventResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        eventService.incrementViewCount(id);
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toEventResponse(event)));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming events")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getUpcomingEvents(
            @RequestParam(defaultValue = "10") int limit) {
        List<Event> events = eventService.getUpcomingEvents(limit);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toEventResponseList(events)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search events")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> searchEvents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Event> events = eventService.searchEvents(keyword, PageRequest.of(page, size));
        Page<EventResponse> response = events.map(dtoMapper::toEventResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/organizer/{organizerId}")
    @Operation(summary = "Get events by organizer")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getEventsByOrganizer(
            @PathVariable Long organizerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        eventService.assertCanAccessOrganizerScope(organizerId, authentication);
        Page<Event> events = eventService.getEventsByOrganizer(organizerId, PageRequest.of(page, size));
        Page<EventResponse> response = events.map(dtoMapper::toEventResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/site/{siteId}")
    @Operation(summary = "Get events by site")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getEventsBySite(
            @PathVariable Long siteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Event> events = eventService.getEventsBySite(siteId, PageRequest.of(page, size));
        Page<EventResponse> response = events.map(dtoMapper::toEventResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Créer un événement")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody EventRequest request,
            Authentication authentication) {
        Event created = eventService.createEvent(
                mapToEvent(request),
                request.getSiteId(),
                request.getOrganizerId(),
                request.getGamificationIds(),
                authentication);
        return ResponseEntity.ok(ApiResponse.success("Événement créé avec succès", dtoMapper.toEventResponse(created)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Mettre à jour un événement")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request,
            Authentication authentication) {
        Event updated = eventService.updateEvent(id, mapToEvent(request), request.getGamificationIds(), authentication);
        return ResponseEntity
                .ok(ApiResponse.success("Événement mis à jour avec succès", dtoMapper.toEventResponse(updated)));
    }

    private Event mapToEvent(EventRequest request) {
        return Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventType(request.getEventType())
                .category(request.getCategory())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .maxParticipants(request.getMaxParticipants())
                .price(request.getPrice())
                .isFree(request.getIsFree())
                .isPublic(request.getIsPublic())
                .requiresApproval(request.getRequiresApproval())
                .images(request.getImages())
                .thumbnail(request.getThumbnail())
                .status(request.getStatus())
                .registrationDeadline(request.getRegistrationDeadline())
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Update event status")
    public ResponseEntity<ApiResponse<EventResponse>> updateEventStatus(
            @PathVariable Long id,
            @RequestParam EventStatus status,
            Authentication authentication) {
        Event updated = eventService.updateEventStatus(id, status, authentication);
        return ResponseEntity.ok(ApiResponse.success("Event status updated", dtoMapper.toEventResponse(updated)));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Publish an event")
    public ResponseEntity<ApiResponse<EventResponse>> publishEvent(
            @PathVariable Long id,
            Authentication authentication) {
        Event published = eventService.publishEvent(id, authentication);
        return ResponseEntity
                .ok(ApiResponse.success("Event published successfully", dtoMapper.toEventResponse(published)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Delete/Cancel an event")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable Long id,
            Authentication authentication) {
        eventService.deleteEvent(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Event cancelled", null));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Bulk delete/cancel events")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteEvents(
            @RequestBody List<Long> ids,
            Authentication authentication) {
        eventService.bulkDeleteEvents(ids, authentication);
        return ResponseEntity.ok(ApiResponse.success("Events cancelled", null));
    }

    @GetMapping("/organizer/{organizerId}/stats")
    @Operation(summary = "Get organizer events statistics")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getOrganizerStats(
            @PathVariable Long organizerId,
            Authentication authentication) {
        eventService.assertCanAccessOrganizerScope(organizerId, authentication);
        Long totalViews = eventService.getTotalViewsByOrganizer(organizerId);
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "totalViews", totalViews != null ? totalViews : 0)));
    }

    @GetMapping("/stats/total-views")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get total views for all events")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getTotalViews() {
        Long totalViews = eventService.getTotalViewsForAllEvents();
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "totalViews", totalViews != null ? totalViews : 0)));
    }
}
