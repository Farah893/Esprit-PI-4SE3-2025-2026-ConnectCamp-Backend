package tn.esprit.projetintegre.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tn.esprit.projetintegre.dto.request.RecommendationAIRequest;
import tn.esprit.projetintegre.dto.response.RecommendationAIResponse;
import tn.esprit.projetintegre.entities.Category;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.entities.Product;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.CategoryRepository;
import tn.esprit.projetintegre.repositories.EventRepository;
import tn.esprit.projetintegre.repositories.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:llama3.2:3b}")
    private String ollamaModel;

    // ─────────────────────────────────────────
    // MAIN — 2 appels Llama
    // ─────────────────────────────────────────
    @Cacheable(value = "recommendations", key = "#eventId", unless = "#result == null")
    public RecommendationAIResponse recommend(Long eventId) {

        // 1. Fetch event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        // 2. Fetch all active categories
        List<Category> allCategories = categoryRepository.findAll()
                .stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .toList();

        // 3. STEP 1 — Ask Llama to pick the best category
        String categoryPrompt = buildCategoryPrompt(event, allCategories);
        String categoryRaw = callOllama(categoryPrompt);
        log.info("Category response: {}", categoryRaw);

        RecommendationAIResponse.CategoryResult bestCategory = parseCategory(categoryRaw);
        if (bestCategory == null) {
            RecommendationAIResponse err = new RecommendationAIResponse();
            err.setEventId(eventId);
            err.setError("Failed to determine best category");
            return err;
        }

        log.info("Best category chosen: {} (id={})", bestCategory.getName(), bestCategory.getId());

        // 4. Fetch products of that category from DB
        List<Product> products = productRepository.findByCategoryId(bestCategory.getId());
        log.info("Found {} products in category {}", products.size(), bestCategory.getName());

        if (products.isEmpty()) {
            RecommendationAIResponse res = new RecommendationAIResponse();
            res.setEventId(eventId);
            res.setBestCategory(bestCategory);
            res.setProducts(new ArrayList<>());
            res.setError("No products found in category: " + bestCategory.getName());
            return res;
        }

        // 5. STEP 2 — Ask Llama to pick TOP 3 products from that list
        String productPrompt = buildProductPrompt(event, bestCategory, products);
        String productRaw = callOllama(productPrompt);
        log.info("Product response: {}", productRaw);

        // 6. Parse and return
        return parseProducts(productRaw, eventId, bestCategory);
    }

    // ─────────────────────────────────────────
    // PROMPT 1 — Choose best category
    // ─────────────────────────────────────────
    private String buildCategoryPrompt(Event event, List<Category> categories) {

        StringBuilder cats = new StringBuilder();
        for (Category c : categories) {
            cats.append("  - id: ").append(c.getId())
                    .append(", name: ").append(c.getName());
            if (c.getDescription() != null && !c.getDescription().isBlank()) {
                cats.append(", description: ").append(c.getDescription());
            }
            cats.append("\n");
        }

        return """
            You are a camping product recommendation AI.

            EVENT:
            - Title: %s
            - Description: %s
            - Type: %s
            - Location: %s

            AVAILABLE CATEGORIES:
            %s

            TASK: Choose the ONE best matching category for this event.

            Return ONLY this JSON (no markdown, no explanation):
            {
              "id": 0,
              "name": ""
            }
            """.formatted(
                event.getTitle(),
                event.getDescription() != null ? event.getDescription() : "",
                event.getEventType() != null ? event.getEventType() : "",
                event.getLocation() != null ? event.getLocation() : "",
                cats.toString()
        );
    }

    // ─────────────────────────────────────────
    // PROMPT 2 — Choose TOP 3 products
    // ─────────────────────────────────────────
    private String buildProductPrompt(Event event,
                                      RecommendationAIResponse.CategoryResult category,
                                      List<Product> products) {

        StringBuilder prods = new StringBuilder();
        for (Product p : products) {
            prods.append("  - id: ").append(p.getId())
                    .append(", name: ").append(p.getName())
                    .append(", price: ").append(p.getPrice());
            if (p.getDescription() != null && !p.getDescription().isBlank()) {
                prods.append(", description: ").append(
                        p.getDescription().length() > 100
                                ? p.getDescription().substring(0, 100)
                                : p.getDescription()
                );
            }
            prods.append("\n");
        }

        return """
            You are a camping product recommendation AI.

            EVENT:
            - Title: %s
            - Description: %s
            - Type: %s
            - Location: %s

            CATEGORY SELECTED: %s

            AVAILABLE PRODUCTS IN THIS CATEGORY:
            %s

            TASK: Select the TOP 3 most relevant products for this event.
            Score each from 0.0 to 1.0 (1.0 = perfect match).

            Return ONLY this JSON (no markdown, no explanation):
            {
              "products": [
                { "productId": 0, "productName": "", "score": 0.0, "reason": "" },
                { "productId": 0, "productName": "", "score": 0.0, "reason": "" },
                { "productId": 0, "productName": "", "score": 0.0, "reason": "" }
              ]
            }

            RULES:
            - Use ONLY products from the list above with their exact id and name
            - Return EXACTLY 3 products
            - Reasons must be under 15 words
            - NEVER invent product ids
            """.formatted(
                event.getTitle(),
                event.getDescription() != null ? event.getDescription() : "",
                event.getEventType() != null ? event.getEventType() : "",
                event.getLocation() != null ? event.getLocation() : "",
                category.getName(),
                prods.toString()
        );
    }

    // ─────────────────────────────────────────
    // CALL OLLAMA
    // ─────────────────────────────────────────
    private String callOllama(String prompt) {
        try {
            WebClient client = WebClient.create(ollamaBaseUrl);

            Map<String, Object> body = Map.of(
                    "model", ollamaModel,
                    "messages", List.of(Map.of(
                            "role", "user",
                            "content", prompt
                    )),
                    "stream", false
            );

            Map response = client.post()
                    .uri("/api/chat")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) throw new RuntimeException("Empty response from Ollama");

            Map<String, Object> message = (Map<String, Object>) response.get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("Ollama call failed: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    // PARSE CATEGORY
    // ─────────────────────────────────────────
    private RecommendationAIResponse.CategoryResult parseCategory(String raw) {
        try {
            String cleaned = extractJson(raw);
            JsonNode root = objectMapper.readTree(cleaned);
            return new RecommendationAIResponse.CategoryResult(
                    root.get("id").asLong(),
                    root.get("name").asText()
            );
        } catch (Exception e) {
            log.error("Failed to parse category: {}", e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────
    // PARSE PRODUCTS
    // ─────────────────────────────────────────
    private RecommendationAIResponse parseProducts(String raw, Long eventId,
                                                   RecommendationAIResponse.CategoryResult category) {
        try {
            String cleaned = extractJson(raw);
            JsonNode root = objectMapper.readTree(cleaned);

            List<RecommendationAIResponse.ProductSuggestion> suggestions = new ArrayList<>();

            for (JsonNode p : root.get("products")) {
                Long productId = p.get("productId").asLong();
                double score = p.get("score").asDouble();
                String reason = p.get("reason").asText();

                // ✅ On ignore le nom retourné par Llama
                // ✅ On fetch le VRAI nom depuis la BD
                Product realProduct = productRepository.findById(productId).orElse(null);

                if (realProduct == null) {
                    log.warn("Llama returned unknown productId: {}, skipping", productId);
                    continue; // ignore les produits inventés
                }

                suggestions.add(new RecommendationAIResponse.ProductSuggestion(
                        realProduct.getId(),
                        realProduct.getName(),      // ← vrai nom depuis BD
                        realProduct.getPrice(),     // ← vrai prix depuis BD (optionnel)
                        score,
                        reason
                ));
            }

            RecommendationAIResponse res = new RecommendationAIResponse();
            res.setEventId(eventId);
            res.setBestCategory(category);
            res.setProducts(suggestions);
            return res;

        } catch (Exception e) {
            log.error("Failed to parse products: {}", e.getMessage());
            RecommendationAIResponse err = new RecommendationAIResponse();
            err.setEventId(eventId);
            err.setBestCategory(category);
            err.setError("Failed to parse products: " + e.getMessage());
            return err;
        }
    }

    // ─────────────────────────────────────────
    // EXTRACT JSON from raw string
    // ─────────────────────────────────────────
    private String extractJson(String raw) {
        String cleaned = raw
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");
        if (start == -1 || end == -1) throw new RuntimeException("No JSON in response");
        return cleaned.substring(start, end + 1);
    }
}
