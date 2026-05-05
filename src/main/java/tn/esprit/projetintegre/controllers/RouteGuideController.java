package tn.esprit.projetintegre.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.request.RouteGuideRequest;
import tn.esprit.projetintegre.dto.response.RouteGuideResponse;
import tn.esprit.projetintegre.services.RouteGuideService;

import java.util.List;

@RestController
@RequestMapping("/api/route-guides")
@RequiredArgsConstructor

public class RouteGuideController {

    private final RouteGuideService routeGuideService;

    @GetMapping("/site/{siteId}")
    public List<RouteGuideResponse> getRoutesBySite(@PathVariable Long siteId) {
        return routeGuideService.getRoutesBySite(siteId);
    }

    @GetMapping("/city/{city}")
    public List<RouteGuideResponse> getRoutesByOriginCity(@PathVariable String city) {
        return routeGuideService.getRoutesByOriginCity(city);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteGuideResponse> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(routeGuideService.getRouteById(id));
    }

    @PostMapping("/site/{siteId}")
    public ResponseEntity<RouteGuideResponse> createRoute(@PathVariable Long siteId,
            @Valid @RequestBody RouteGuideRequest request) {
        return ResponseEntity.ok(routeGuideService.createRoute(siteId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteGuideResponse> updateRoute(@PathVariable Long id,
            @Valid @RequestBody RouteGuideRequest request) {
        return ResponseEntity.ok(routeGuideService.updateRoute(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeGuideService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }
}
