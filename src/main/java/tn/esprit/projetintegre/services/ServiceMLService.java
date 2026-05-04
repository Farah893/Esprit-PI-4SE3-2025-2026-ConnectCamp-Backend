package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tn.esprit.projetintegre.dto.ml.ServiceMLRequest;
import tn.esprit.projetintegre.dto.ml.ServiceMLResponse;
import tn.esprit.projetintegre.entities.CampingService;

import java.time.LocalDateTime;

/**
 * Integrates the Python ML server for CampingService predictions.
 *
 * Three predictions are exposed:
 *   1. predictRating(...)   → POST /api/ml/services/predict-rating
 *   2. predictDemand(...)   → POST /api/ml/services/predict-demand
 *   3. predictFull(...)     → POST /api/ml/services/predict  (rating + demand)
 *
 * If the ML server is unreachable a graceful fallback is returned.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceMLService {

    private final RestTemplate restTemplate;

    @Value("${ml.api.base-url:http://localhost:8000}")
    private String mlBaseUrl;

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Predict the expected rating for a new or updated camping service.
     */
    public ServiceMLResponse predictRating(ServiceMLRequest request) {
        return call("/api/ml/services/predict-rating", request);
    }

    /**
     * Predict the demand level (LOW/MEDIUM/HIGH) for a camping service.
     * Pass the current or predicted rating in the request.
     */
    public ServiceMLResponse predictDemand(ServiceMLRequest request) {
        return call("/api/ml/services/predict-demand", request);
    }

    /**
     * Combined endpoint: rating + demand in one round-trip.
     */
    public ServiceMLResponse predictFull(ServiceMLRequest request) {
        return call("/api/ml/services/predict", request);
    }

    /**
     * Convenience: build the ML request from an existing CampingService entity.
     * Season is inferred from the current month.
     */
    public ServiceMLResponse predictForService(CampingService service) {
        int season = currentSeason();

        ServiceMLRequest req = ServiceMLRequest.builder()
                .serviceType(service.getType() != null ? service.getType().name() : "OTHER")
                .priceEur(service.getPrice() != null ? service.getPrice().doubleValue() : 50.0)
                .durationMinutes(service.getDuration() != null ? service.getDuration() : 120)
                .maxCapacity(service.getMaxCapacity() != null ? service.getMaxCapacity() : 10)
                .season(season)
                .isCamperOnly(Boolean.TRUE.equals(service.getIsCamperOnly()))
                .isOrganizerService(Boolean.TRUE.equals(service.getIsOrganizerService()))
                .locationScore(5)   // default — no location_score field on entity
                .providerExperienceYears(3)
                .reviewCount(service.getReviewCount() != null ? service.getReviewCount() : 0)
                .rating(service.getRating() != null ? service.getRating().doubleValue() : null)
                .build();

        return predictFull(req);
    }

    // ── Internal ──────────────────────────────────────────────────────────

    private ServiceMLResponse call(String endpoint, ServiceMLRequest body) {
        String url = mlBaseUrl + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ServiceMLRequest> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<ServiceMLResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, ServiceMLResponse.class);

            ServiceMLResponse result = response.getBody();
            if (result == null) {
                return errorFallback("ML server returned an empty response");
            }
            return result;

        } catch (RestClientException e) {
            log.warn("ML server unreachable at {}: {}", url, e.getMessage());
            return errorFallback("ML server unavailable: " + e.getMessage());
        }
    }

    private ServiceMLResponse errorFallback(String message) {
        ServiceMLResponse fallback = new ServiceMLResponse();
        fallback.setError(true);
        fallback.setErrorMessage(message);
        return fallback;
    }

    /** Derive season (1=spring, 2=summer, 3=autumn, 4=winter) from current month. */
    private int currentSeason() {
        int month = LocalDateTime.now().getMonthValue();
        if (month >= 3 && month <= 5)  return 1; // spring
        if (month >= 6 && month <= 8)  return 2; // summer
        if (month >= 9 && month <= 11) return 3; // autumn
        return 4;                                 // winter
    }
}
