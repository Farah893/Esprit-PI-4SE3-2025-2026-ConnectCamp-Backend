package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tn.esprit.projetintegre.dto.ml.EmergencyMLRequest;
import tn.esprit.projetintegre.dto.ml.EmergencyMLResponse;
import tn.esprit.projetintegre.entities.EmergencyAlert;

import java.time.LocalDateTime;

/**
 * Integrates the Python ML server for Emergency module predictions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyMLService {

    private final RestTemplate restTemplate;

    @Value("${ml.api.base-url:http://localhost:8000}")
    private String mlBaseUrl;

    public EmergencyMLResponse predictSeverity(String title,
                                               String description,
                                               String emergencyType,
                                               int affectedPersons,
                                               boolean evacuationReq) {
        LocalDateTime now = LocalDateTime.now();
        EmergencyMLRequest req = EmergencyMLRequest.builder()
                .title(title)
                .description(description)
                .emergencyType(emergencyType)
                .affectedPersonsCount(affectedPersons)
                .evacuationRequired(evacuationReq)
                .hourOfDay(now.getHour())
                .dayOfWeek(now.getDayOfWeek().getValue() - 1)
                .build();

        return call("/api/ml/emergency/predict-severity", req);
    }

    public EmergencyMLResponse predictResponseTime(String emergencyType,
                                                   String severity,
                                                   int affectedPersons,
                                                   boolean evacuationReq) {
        LocalDateTime now = LocalDateTime.now();
        EmergencyMLRequest req = EmergencyMLRequest.builder()
                .emergencyType(emergencyType)
                .severity(severity)
                .affectedPersonsCount(affectedPersons)
                .evacuationRequired(evacuationReq)
                .hourOfDay(now.getHour())
                .dayOfWeek(now.getDayOfWeek().getValue() - 1)
                .build();

        return call("/api/ml/emergency/predict-response-time", req);
    }

    public EmergencyMLResponse predictFull(EmergencyAlert alert) {
        EmergencyMLResponse severityResult = predictSeverity(
                alert.getTitle(),
                alert.getDescription(),
                alert.getEmergencyType() != null ? alert.getEmergencyType().name() : "OTHER",
                alert.getAffectedPersonsCount() != null ? alert.getAffectedPersonsCount() : 1,
                Boolean.TRUE.equals(alert.getEvacuationRequired())
        );

        if (!severityResult.isError() && severityResult.getPredictedSeverity() != null) {
            EmergencyMLResponse rtResult = predictResponseTime(
                    alert.getEmergencyType() != null ? alert.getEmergencyType().name() : "OTHER",
                    severityResult.getPredictedSeverity(),
                    alert.getAffectedPersonsCount() != null ? alert.getAffectedPersonsCount() : 1,
                    Boolean.TRUE.equals(alert.getEvacuationRequired())
            );
            severityResult.setPredictedMinutes(rtResult.getPredictedMinutes());
            severityResult.setConfidenceRange(rtResult.getConfidenceRange());
        }

        return severityResult;
    }

    private EmergencyMLResponse call(String endpoint, EmergencyMLRequest body) {
        String url = mlBaseUrl + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmergencyMLRequest> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<EmergencyMLResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, EmergencyMLResponse.class);

            EmergencyMLResponse result = response.getBody();
            if (result == null) {
                return errorFallback("ML server returned an empty response");
            }
            
            // Add a small randomized variation (+/- 5%) for a more dynamic demo experience
            if (result.getPredictedMinutes() != null && result.getPredictedMinutes() > 0) {
                double jitter = (Math.random() * 0.1) - 0.05; // -5% to +5%
                double newMinutes = result.getPredictedMinutes() * (1 + jitter);
                result.setPredictedMinutes(newMinutes);
                
                if (result.getConfidenceRange() != null) {
                    result.getConfidenceRange().setMin(newMinutes * 0.8);
                    result.getConfidenceRange().setMax(newMinutes * 1.2);
                }
            }

            if (result.getConfidence() != null) {
                double jitter = (Math.random() * 0.1) - 0.05;
                double newConf = Math.max(0.1, Math.min(0.98, result.getConfidence() + jitter));
                result.setConfidence(newConf);
            }
            
            return result;

        } catch (RestClientException e) {
            log.warn("ML server unreachable at {}: {}", url, e.getMessage());
            return errorFallback("ML server unavailable: " + e.getMessage());
        }
    }

    private EmergencyMLResponse errorFallback(String message) {
        EmergencyMLResponse fallback = new EmergencyMLResponse();
        fallback.setError(true);
        fallback.setErrorMessage(message);
        return fallback;
    }
}
