package tn.esprit.projetintegre.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.entities.Product;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.ProductRepository;
import tn.esprit.projetintegre.services.PricePredictionService;

@RestController
@RequestMapping("/api/price-prediction")
@RequiredArgsConstructor
public class PricePredictionController {

    private final ProductRepository productRepository;
    private final PricePredictionService pricePredictionService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> predictForProduct(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        PricePredictionService.PricePredictionResult result = pricePredictionService.predict(product);

        if (result == null) {
            return ResponseEntity.status(503).body("ML service unavailable");
        }
        return ResponseEntity.ok(result);
    }
}
