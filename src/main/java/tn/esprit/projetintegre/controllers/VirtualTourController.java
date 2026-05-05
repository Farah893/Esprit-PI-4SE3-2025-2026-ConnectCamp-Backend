package tn.esprit.projetintegre.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.request.VirtualTourRequest;
import tn.esprit.projetintegre.dto.response.VirtualTourResponse;
import tn.esprit.projetintegre.services.VirtualTourService;

import java.util.List;

@RestController
@RequestMapping("/api/virtual-tours")
@RequiredArgsConstructor

public class VirtualTourController {

    private final VirtualTourService virtualTourService;

    @GetMapping("/site/{siteId}")
    public List<VirtualTourResponse> getBySite(@PathVariable Long siteId) {
        return virtualTourService.getToursBySite(siteId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VirtualTourResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(virtualTourService.getTourById(id));
    }

    @PostMapping("/site/{siteId}")
    public ResponseEntity<VirtualTourResponse> create(@PathVariable Long siteId,
            @Valid @RequestBody VirtualTourRequest request) {
        return ResponseEntity.ok(virtualTourService.createTour(siteId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VirtualTourResponse> update(@PathVariable Long id,
            @Valid @RequestBody VirtualTourRequest request) {
        return ResponseEntity.ok(virtualTourService.updateTour(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        virtualTourService.deleteTour(id);
        return ResponseEntity.noContent().build();
    }
}
