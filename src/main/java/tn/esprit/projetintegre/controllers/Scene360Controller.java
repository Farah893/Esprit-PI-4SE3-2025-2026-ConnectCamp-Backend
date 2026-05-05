package tn.esprit.projetintegre.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.request.Scene360Request;
import tn.esprit.projetintegre.dto.response.Scene360Response;
import tn.esprit.projetintegre.services.Scene360Service;

import java.util.List;

@RestController
@RequestMapping("/api/scenes-360")
@RequiredArgsConstructor

public class Scene360Controller {

    private final Scene360Service scene360Service;

    @GetMapping("/tour/{tourId}")
    public List<Scene360Response> getScenesByTour(@PathVariable Long tourId) {
        return scene360Service.getScenesByTour(tourId);
    }

    @PostMapping("/tour/{tourId}")
    public ResponseEntity<Scene360Response> addScene(@PathVariable Long tourId,
            @Valid @RequestBody Scene360Request request) {
        return ResponseEntity.ok(scene360Service.addScene(tourId, request));
    }

    @PutMapping("/{sceneId}")
    public ResponseEntity<Scene360Response> updateScene(@PathVariable Long sceneId,
            @Valid @RequestBody Scene360Request request) {
        return ResponseEntity.ok(scene360Service.updateScene(sceneId, request));
    }

    @DeleteMapping("/{sceneId}")
    public ResponseEntity<Void> deleteScene(@PathVariable Long sceneId) {
        scene360Service.deleteScene(sceneId);
        return ResponseEntity.noContent().build();
    }
}
