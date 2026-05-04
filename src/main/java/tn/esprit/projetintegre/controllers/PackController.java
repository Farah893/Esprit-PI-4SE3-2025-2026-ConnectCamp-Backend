package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.BundleOptimizerResultDTO;
import tn.esprit.projetintegre.dto.PackDTO;
import tn.esprit.projetintegre.dto.PackServiceStatsDTO;
import tn.esprit.projetintegre.dto.PackValueRankDTO;
import tn.esprit.projetintegre.dto.PageResponse;
import tn.esprit.projetintegre.enums.PackType;
import tn.esprit.projetintegre.enums.ServiceType;
import tn.esprit.projetintegre.services.PackService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/packs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@Tag(name = "Packs", description = "API pour la gestion des packs de services")
public class PackController {

    private final PackService packService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un pack")
    public ResponseEntity<ApiResponse<PackDTO.Response>> createPack(
            @Valid @RequestBody PackDTO.CreateRequest request) {
        PackDTO.Response response = packService.createPack(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pack créé avec succès", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un pack par ID")
    public ResponseEntity<ApiResponse<PackDTO.Response>> getById(@PathVariable Long id) {
        PackDTO.Response response = packService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Obtenir tous les packs actifs")
    public ResponseEntity<ApiResponse<PageResponse<PackDTO.Response>>> getAllActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        var result = packService.getAllActive(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtenir tous les packs pour l'admin")
    public ResponseEntity<ApiResponse<PageResponse<PackDTO.Response>>> getAllAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        var result = packService.getAllAdmin(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un pack")
    public ResponseEntity<ApiResponse<PackDTO.Response>> updatePack(
            @PathVariable Long id,
            @Valid @RequestBody PackDTO.UpdateRequest request) {
        PackDTO.Response response = packService.updatePack(id, request);
        return ResponseEntity.ok(ApiResponse.success("Pack mis à jour avec succès", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un pack")
    public ResponseEntity<ApiResponse<Void>> deletePack(@PathVariable Long id) {
        packService.deletePack(id);
        return ResponseEntity.ok(ApiResponse.success("Pack supprimé avec succès", null));
    }

    // ── Analytics & Advanced features ────────────────────────────────────

    @GetMapping("/stats/with-services")
    @Operation(summary = "Packs actifs avec statistiques de services")
    public ResponseEntity<ApiResponse<List<PackServiceStatsDTO>>> getPacksWithServiceStats() {
        List<PackServiceStatsDTO> stats = packService.getActivePacksWithServiceStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/value-ranking")
    @Operation(summary = "Classement des packs par indice de valeur")
    public ResponseEntity<ApiResponse<List<PackValueRankDTO>>> getPackValueRanking() {
        List<PackValueRankDTO> result = packService.getPackValueRanking();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filtrer les packs par site et type de service")
    public ResponseEntity<ApiResponse<List<PackDTO.Response>>> filterPacks(
            @RequestParam String siteName,
            @RequestParam ServiceType serviceType) {
        List<PackDTO.Response> result = packService.filterPacks(siteName, serviceType);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/bundle-optimizer")
    @Operation(summary = "Combinaison optimale de packs dans un budget")
    public ResponseEntity<ApiResponse<BundleOptimizerResultDTO>> getBundleOptimizer(
            @RequestParam BigDecimal budget,
            @RequestParam(required = false) Integer persons) {
        BundleOptimizerResultDTO result = packService.optimizeBundle(budget, persons);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/quality-metrics")
    @Operation(summary = "Métriques de qualité et sentiment des avis par pack")
    public ResponseEntity<ApiResponse<List<tn.esprit.projetintegre.dto.PackQualityDTO>>> getPackQualityMetrics() {
        List<tn.esprit.projetintegre.dto.PackQualityDTO> result = packService.getPackQualityMetrics();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer ou désactiver un pack")
    public ResponseEntity<ApiResponse<Void>> updateActiveStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        packService.updateActiveStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Statut mis à jour avec succès", null));
    }
}
