package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.PageResponse;
import tn.esprit.projetintegre.dto.ml.ServiceMLRequest;
import tn.esprit.projetintegre.dto.ml.ServiceMLResponse;
import tn.esprit.projetintegre.dto.request.CampingServiceRequest;
import tn.esprit.projetintegre.dto.response.CampingServiceResponse;
import tn.esprit.projetintegre.entities.CampingService;
import tn.esprit.projetintegre.enums.ServiceType;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.CampingServiceService;
import tn.esprit.projetintegre.services.ServiceMLService;

import java.util.List;

@RestController
@RequestMapping("/api/camping-services")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://lively-wave-019e62b03.7.azurestaticapps.net"}, allowCredentials = "true")
@Tag(name = "Camping Services", description = "Camping service management APIs")
public class CampingServiceController {

    private final CampingServiceService campingServiceService;
    private final DtoMapper dtoMapper;
    private final ServiceMLService serviceMLService;

    @GetMapping
    @Transactional
    @Operation(summary = "Get active camping services paginated")
    public ResponseEntity<ApiResponse<PageResponse<CampingServiceResponse>>> getAllServices(Pageable pageable) {
        Page<CampingService> page = campingServiceService.getActiveServices(pageable);
        Page<CampingServiceResponse> response = page.map(dtoMapper::toCampingServiceResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get camping service by ID")
    public ResponseEntity<ApiResponse<CampingServiceResponse>> getServiceById(@PathVariable Long id) {
        CampingService service = campingServiceService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toCampingServiceResponse(service)));
    }

    @GetMapping("/organizer")
    @Operation(summary = "Get services specifically for organizers (B2B)")
    public ResponseEntity<ApiResponse<PageResponse<CampingServiceResponse>>> getOrganizerServices(Pageable pageable) {
        Page<CampingService> page = campingServiceService.getOrganizerServices(pageable);
        Page<CampingServiceResponse> response = page.map(dtoMapper::toCampingServiceResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @PostMapping
    @Operation(summary = "Create a new camping service")
    public ResponseEntity<ApiResponse<CampingServiceResponse>> createService(
            @Valid @RequestBody CampingServiceRequest request,
            @RequestParam Long providerId,
            @RequestParam(required = false) Long siteId) {
        CampingService service = toEntity(request);
        CampingService created = campingServiceService.createService(service, providerId, siteId);
        return ResponseEntity.ok(ApiResponse.success("Camping service created successfully",
                dtoMapper.toCampingServiceResponse(created)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a camping service")
    public ResponseEntity<ApiResponse<CampingServiceResponse>> updateService(
            @PathVariable Long id,
            @Valid @RequestBody CampingServiceRequest request) {
        CampingService serviceDetails = toEntity(request);
        CampingService updated = campingServiceService.updateService(id, serviceDetails);
        return ResponseEntity.ok(ApiResponse.success("Camping service updated successfully",
                dtoMapper.toCampingServiceResponse(updated)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a camping service")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id) {
        campingServiceService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success("Camping service deleted successfully", null));
    }

    @GetMapping("/analytics/at-risk")
    @Operation(summary = "Get services at risk (Low rating + High demand)")
    public ResponseEntity<ApiResponse<List<Object[]>>> getAtRiskServices() {
        List<Object[]> result = campingServiceService.getAtRiskServices();
        return ResponseEntity.ok(ApiResponse.success("At-risk analytics generated successfully", result));
    }

    // ── ML Prediction endpoints ───────────────────────────────────────────

    @GetMapping("/{id}/ml/predict")
    @Operation(summary = "Predict rating + demand for an existing service (ML)")
    public ResponseEntity<ApiResponse<ServiceMLResponse>> predictForService(@PathVariable Long id) {
        CampingService service = campingServiceService.getServiceById(id);
        ServiceMLResponse result = serviceMLService.predictForService(service);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/ml/predict")
    @Operation(summary = "Predict rating + demand from raw ML request")
    public ResponseEntity<ApiResponse<ServiceMLResponse>> predictFull(
            @Valid @RequestBody ServiceMLRequest request) {
        ServiceMLResponse result = serviceMLService.predictFull(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private CampingService toEntity(CampingServiceRequest request) {
        return CampingService.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .price(request.getPrice())
                .pricingUnit(request.getPricingUnit())
                .images(request.getImages())
                .isActive(request.getIsActive())
                .isAvailable(request.getIsAvailable())
                .isCamperOnly(request.getIsCamperOnly())
                .isOrganizerService(request.getIsOrganizerService())
                .maxCapacity(request.getMaxCapacity())
                .duration(request.getDuration())
                .build();
    }
}
