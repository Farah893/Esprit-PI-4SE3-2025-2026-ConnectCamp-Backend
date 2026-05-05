package tn.esprit.projetintegre.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.request.CertificationItemRequest;
import tn.esprit.projetintegre.dto.response.CertificationItemResponse;
import tn.esprit.projetintegre.services.CertificationItemService;

import java.util.List;

@RestController
@RequestMapping("/api/certification-items")
@RequiredArgsConstructor

public class CertificationItemController {

    private final CertificationItemService certificationItemService;

    @GetMapping("/certification/{certificationId}")
    public List<CertificationItemResponse> getItemsByCertification(@PathVariable Long certificationId) {
        return certificationItemService.getItemsByCertification(certificationId);
    }

    @PostMapping("/certification/{certificationId}")
    public ResponseEntity<CertificationItemResponse> addItem(@PathVariable Long certificationId,
            @Valid @RequestBody CertificationItemRequest request) {
        return ResponseEntity.ok(certificationItemService.addItem(certificationId, request));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<CertificationItemResponse> updateItem(@PathVariable Long itemId,
            @Valid @RequestBody CertificationItemRequest request) {
        return ResponseEntity.ok(certificationItemService.updateItem(itemId, request));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        certificationItemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
