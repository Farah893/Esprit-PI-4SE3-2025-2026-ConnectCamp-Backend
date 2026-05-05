// OrganizerController.java
package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.request.OrganizerRequest;
import tn.esprit.projetintegre.dto.response.OrganizerResponse;
import tn.esprit.projetintegre.entities.Organizer;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.repositories.OrganizerRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organizers")
@RequiredArgsConstructor

@Tag(name = "Organizers", description = "Organizer management APIs")
public class OrganizerController {

    private final OrganizerRepository organizerRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all organizers")
    public ResponseEntity<ApiResponse<List<OrganizerResponse>>> getAllOrganizers() {
        List<OrganizerResponse> list = organizerRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organizer by ID")
    public ResponseEntity<ApiResponse<OrganizerResponse>> getById(@PathVariable Long id) {
        Organizer org = organizerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));
        return ResponseEntity.ok(ApiResponse.success(toResponse(org)));
    }

    @PostMapping
    @Operation(summary = "Create organizer")
    public ResponseEntity<ApiResponse<OrganizerResponse>> create(
            @Valid @RequestBody OrganizerRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (organizerRepository.existsByUser_Id(request.getUserId())) {
            throw new RuntimeException("User already has an organizer");
        }

        Organizer organizer = Organizer.builder()
                .companyName(request.getCompanyName())
                .description(request.getDescription())
                .logo(request.getLogo())
                .banner(request.getBanner())
                .website(request.getWebsite())
                .siretNumber(request.getSiretNumber())
                .address(request.getAddress())
                .phone(request.getPhone())
                .user(user)
                .build();

        Organizer saved = organizerRepository.save(organizer);
        return ResponseEntity.ok(ApiResponse.success("Organizer created", toResponse(saved)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organizer")
    public ResponseEntity<ApiResponse<OrganizerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody OrganizerRequest request) {
        Organizer org = organizerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        org.setCompanyName(request.getCompanyName());
        org.setDescription(request.getDescription());
        org.setLogo(request.getLogo());
        org.setBanner(request.getBanner());
        org.setWebsite(request.getWebsite());
        org.setSiretNumber(request.getSiretNumber());
        org.setAddress(request.getAddress());
        org.setPhone(request.getPhone());

        return ResponseEntity.ok(ApiResponse.success("Organizer updated", toResponse(organizerRepository.save(org))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete organizer")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        organizerRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Organizer deleted", null));
    }

    private OrganizerResponse toResponse(Organizer o) {
        return OrganizerResponse.builder()
                .id(o.getId())
                .companyName(o.getCompanyName())
                .description(o.getDescription())
                .logo(o.getLogo())
                .banner(o.getBanner())
                .website(o.getWebsite())
                .siretNumber(o.getSiretNumber())
                .address(o.getAddress())
                .phone(o.getPhone())
                .rating(o.getRating())
                .reviewCount(o.getReviewCount())
                .totalEvents(o.getTotalEvents())
                .verified(o.getVerified())
                .active(o.getActive())
                .userId(o.getUser() != null ? o.getUser().getId() : null)
                .userName(o.getUser() != null ? o.getUser().getName() : null)
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
