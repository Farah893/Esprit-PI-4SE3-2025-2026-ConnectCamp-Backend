package tn.esprit.projetintegre.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TrackingApiService {

    @Value("${tracking.api.url:https://api.17track.net}")
    private String apiUrl;

    @Value("${tracking.api.key}")
    private String apiKey;

    private WebClient getClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("17token", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    /**
     * Enregistre un colis sur 17TRACK.
     * Après cet appel, le numéro apparaît dans le dashboard 17TRACK.
     *
     * Réponse succès:
     * {"code":0,"data":{"accepted":[{"number":"CC123456789FR"}],"rejected":[]}}
     *
     * Code -18019901 = Already registered → pas une erreur, c'est OK
     */
    public String registerTracking(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isBlank()) return null;

        log.info("════════════════════════════════════════");
        log.info("17TRACK → REGISTER: {}", trackingNumber);

        try {
            String response = getClient().post()
                    .uri("/track/v2.4/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(List.of(Map.of("number", trackingNumber)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("17TRACK REGISTER response: {}", response);

            if (response != null) {
                if (response.contains("-18019901")) {
                    log.info("ℹ️  17TRACK: {} déjà enregistré (OK)", trackingNumber);
                } else if (response.contains("\"accepted\"") && response.contains(trackingNumber)) {
                    log.info("✅ 17TRACK: {} enregistré → visible sur dashboard 17track.net", trackingNumber);
                }
            }
            log.info("════════════════════════════════════════");
            return response;

        } catch (WebClientResponseException e) {
            log.error("17TRACK REGISTER HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("17TRACK REGISTER erreur: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Récupère les infos de tracking depuis 17TRACK.
     * Events dans: data.accepted[0].track.w1.z1
     * Champs: "a"=status, "b"=datetime, "c"=location, "d"=description
     */
    public String getTrackingInfo(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isBlank()) return null;

        log.info("17TRACK → GET INFO: {}", trackingNumber);

        try {
            String response = getClient().post()
                    .uri("/track/v2.4/gettrackinfo")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(List.of(Map.of("number", trackingNumber)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("17TRACK GET INFO response: {}", response);
            return response;

        } catch (WebClientResponseException e) {
            log.error("17TRACK GET INFO HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("17TRACK GET INFO erreur: {}", e.getMessage());
            return null;
        }
    }
}
