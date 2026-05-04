package tn.esprit.projetintegre.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.projetintegre.dto.*;
import tn.esprit.projetintegre.entities.CampingService;
import tn.esprit.projetintegre.entities.Site;
import tn.esprit.projetintegre.repositories.CampingServiceRepository;
import tn.esprit.projetintegre.repositories.ServiceReviewRepository;
import tn.esprit.projetintegre.repositories.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.model:llama-3.1-8b-instant}")
    private String model;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final EmergencyAlertService alertService;
    private final PackService packService;
    private final NasaEonetService nasaEonetService;
    private final SiteRepository siteRepository;
    private final CampingServiceRepository campingServiceRepository;
    private final ServiceReviewRepository serviceReviewRepository;
    private final tn.esprit.projetintegre.repositories.PackRepository packRepository;

    // ─────────────────────────────────────────────────────────────────
    //  FEATURE 1 — Emergency SITREP
    // ─────────────────────────────────────────────────────────────────

    public AiSitrepResponseDTO generateSitrep() {
        List<EmergencyAlertDTO.Response> alerts = alertService.getActiveAlerts();

        String alertsJson = toJson(alerts.stream().map(a -> Map.of(
                "id",     a.getId(),
                "title",  a.getTitle(),
                "type",   a.getEmergencyType(),
                "severity", a.getSeverity(),
                "status", a.getStatus(),
                "site",   a.getSiteName() != null ? a.getSiteName() : "unknown",
                "reportedAt", a.getReportedAt()
        )).collect(Collectors.toList()));

        String prompt = """
                You are an emergency coordinator AI for a Tunisian camping resort.

                Analyze the following %d active emergency alerts and generate a SITREP.

                Alerts (JSON):
                %s

                Respond ONLY with valid JSON (no markdown, no extra text), using exactly this structure:
                {
                  "threatLevel": "SAFE|WATCH|DANGER|CRITICAL",
                  "summary": "2-3 sentence narrative in English",
                  "priorities": ["Action 1", "Action 2", "Action 3"],
                  "escalationRisk": 0-100,
                  "duplicateWarning": "description or null",
                  "recommendedProtocol": "protocol name or null"
                }
                """.formatted(alerts.size(), alertsJson);

        String raw = callGroq(prompt);
        return parseSitrep(raw, alerts.size());
    }

    // ─────────────────────────────────────────────────────────────────
    //  FEATURE 1.5 — Review Authenticity Guard
    // ─────────────────────────────────────────────────────────────────

    public ReviewAnalysisResultDTO validateReviewConsistency(int rating, String text) {
        if (text == null || text.length() < 10) {
            return ReviewAnalysisResultDTO.builder().consistent(true).reason(null).build();
        }

        String prompt = """
                You are a Review Authenticity Guard for a camping resort. 
                Analyze the COHERENCE between the user's star rating (%d/5) and their text comment.
                
                CRITICAL DETECTION RULES:
                1. DETECT CONTRADICTION: If the text is overwhelmingly positive (e.g., 'Perfect', 'Loved it', 'Amazing') but the rating is low (1 or 2 stars), mark as inconsistent.
                2. DETECT SARCASM/BOMBING: If the text is very negative (e.g., 'Terrible', 'Dirty', 'Avoid', 'Worst experience') but the rating is high (4 or 5 stars), mark as inconsistent.
                3. NEUTRALITY: If the text is mixed or neutral, it is usually consistent.
                
                Review Text: "%s"
                Star Rating: %d/5
                
                Respond ONLY with JSON: {"consistent": true|false, "reason": "short explanation of the contradiction or match"}
                """.formatted(rating, text.replace("\"", "'"), rating);

        try {
            String raw = callGroq(prompt);
            JsonNode node = objectMapper.readTree(extractJson(raw));
            boolean consistent = node.path("consistent").asBoolean(true);
            String reason = node.path("reason").asText(null);
            
            if (!consistent) {
                log.warn("AI detected inconsistent review: Rating={}, Text='{}', Reason='{}'", 
                         rating, text, reason);
            }
            return ReviewAnalysisResultDTO.builder().consistent(consistent).reason(reason).build();
        } catch (Exception e) {
            log.error("Consistency check failed, defaulting to true: {}", e.getMessage());
            return ReviewAnalysisResultDTO.builder().consistent(true).reason(null).build();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  FEATURE 2 — Pack Natural Language Advisor
    // ─────────────────────────────────────────────────────────────────

    public AiPackAdvisorResponseDTO advisePacks(AiPackAdvisorRequestDTO request) {
        try {
        List<PackQualityDTO> packs = packService.getPackQualityMetrics();
        System.out.println("DEBUG: AI Advisor found " + packs.size() + " packs to analyze");

        String packsJson = toJson(packs.stream().map(p -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id",          p.getPackId());
                m.put("name",        p.getPackName());
                m.put("place",       (p.getSiteName() != null ? p.getSiteName() : "Unknown") + " (" + (p.getSiteLocation() != null ? p.getSiteLocation() : "") + ")");
                m.put("price",       p.getPrice());
                m.put("rating",      p.getAverageRating() != null ? p.getAverageRating() : 0.0);
                m.put("trustScore",  p.getTrustScore() + "/100");
                m.put("reviews",     p.getTotalReviews() != null ? p.getTotalReviews() : 0);
                m.put("quality",     p.getAvgServiceQuality() != null ? p.getAvgServiceQuality() : 0.0);
                m.put("valueRank",   p.getAvgValueForMoney() != null ? p.getAvgValueForMoney() : 0.0);
                m.put("userPros",    p.getTopPros() != null ? p.getTopPros() : List.of());
                m.put("userCons",    p.getTopCons() != null ? p.getTopCons() : List.of());
                return m;
        }).collect(Collectors.toList()));

        // ── Auto-fetch NASA EONET si les coordonnées GPS sont fournies ──────────
        List<String> resolvedEonetEvents = request.getEonetEvents();
        String locationCtx = "Location: not specified";

        if (request.getLatitude() != null && request.getLongitude() != null) {
            resolvedEonetEvents = nasaEonetService.getEventTitlesNear(
                    request.getLatitude(), request.getLongitude());
            locationCtx = "User GPS location: lat=" + request.getLatitude()
                    + ", lon=" + request.getLongitude();

            // --- GPS-to-Site Link (Proximity detection) ---
            List<Site> sites = siteRepository.findListedActiveSites();
            boolean siteFound = false;
            for (Site s : sites) {
                // 1. Essayer par coordonnées GPS
                if (s.getLatitude() != null && s.getLongitude() != null) {
                    double dist = calculateDistance(request.getLatitude(), request.getLongitude(), 
                                                   s.getLatitude(), s.getLongitude());
                    if (dist < 20.0) {
                        locationCtx += " (STATIONARY NEAR SITE: " + s.getName() + " in " + s.getCity() + ")";
                        siteFound = true;
                        break;
                    }
                }
            }
            
            // 2. Fallback par Ville (si pas trouvé par GPS)
            if (!siteFound) {
                locationCtx += " (Searching for sites in nearby cities...)";
            }
        }

        String eonetCtx = (resolvedEonetEvents != null && !resolvedEonetEvents.isEmpty())
                ? "ACTIVE NASA EONET EVENTS near user: " + String.join(", ", resolvedEonetEvents)
                : "No active natural events detected near the user's location.";

        String siteCtx = request.getSiteRiskLevel() != null
                ? "Current site risk level: " + request.getSiteRiskLevel()
                : "Site risk level: not provided.";

        // --- Weather Simulation (Weather-Smart Bundling) ---
        String weatherCtx = "Forecast: Scattered clouds, light wind, 24°C (Safe for camping).";
        if (eonetCtx.contains("Fire") || eonetCtx.contains("Heat")) {
            weatherCtx = "Forecast: Extreme heat warning, high UV index, risk of dry winds.";
        } else if (eonetCtx.contains("Flood") || eonetCtx.contains("Storm")) {
            weatherCtx = "Forecast: Heavy rain expected, potential for thunderstorms and mud.";
        }

        String safeWeatherCtx = weatherCtx.replace("%", "%%");
        String safeEonetCtx  = eonetCtx.replace("%", "%%");
        String safeSiteCtx   = siteCtx.replace("%", "%%");
        String safePacksJson = packsJson.replace("%", "%%");
        String safeUserQuery = request.getUserQuery().replace("%", "%%");
        String safeBudget    = (request.getBudget()  != null ? String.valueOf(request.getBudget())  : "flexible");
        String safePersons   = (request.getPersons() != null ? String.valueOf(request.getPersons()) : "not specified");

        String prompt = ("You are a specialized camping pack advisor AI for a Tunisian camping resort.\n"
                + "Your role is to help users choose packs based on QUALITY, PLACE, and RATINGS, not just price.\n"
                + "IMPORTANT: Always respond in ENGLISH.\n\n"
                + "STRICT RULE: If the user question is NOT related to camping packs, services, bookings, "
                + "budget or nature safety, respond with ONLY this JSON:\n"
                + "{\"offTopic\":true,\"recommendation\":\"I am only available to help you choose "
                + "camping packs. Please ask me a question about our packs, services or budget.\","
                + "\"suggestedPackIds\":[],\"suggestedPackNames\":[],\"totalEstimatedCost\":0,"
                + "\"savingsEstimate\":0,\"safetyNote\":null,\"confidence\":\"HIGH\"}\n\n"
                + "Context:\n"
                + "- Weather Forecast (Weather-Smart): %s\n"
                + "- Safety Data (NASA EONET): %s\n"
                + "- Local Site Context: %s\n"
                + "- User Proximity: %s\n"
                + "- Budget: " + safeBudget + " TND\n"
                + "- Group size: " + safePersons + " persons\n\n"
                + "Available camping packs with QUALITY & REVIEWS (JSON):\n%s\n\n"
                + "User request: \"%s\"\n\n"
                + "Instructions:\n"
                + "1. Prioritize packs with high 'trustScore' and positive 'userPros'.\n"
                + "2. Mention the specific 'place' in your recommendation to show coherence.\n"
                + "3. If the User Proximity shows they are near a specific site, prioritize packs from that site.\n"
                + "4. WEATHER-SMART BUNDLING: Adjust recommendations based on the forecast.\n"
                + "5. RELATION WITH NASA EONET: If there are active natural events, warn the user.\n"
                + "6. If no packs match, explain why.\n\n"
                + "Respond ONLY with valid JSON (no markdown, no extra text). Write the recommendation field in ENGLISH:\n"
                + "{\n"
                + "  \"offTopic\": false,\n"
                + "  \"recommendation\": \"your coherent recommendation in English...\",\n"
                + "  \"suggestedPackIds\": [...],\n"
                + "  \"suggestedPackNames\": [...],\n"
                + "  \"totalEstimatedCost\": 0,\n"
                + "  \"savingsEstimate\": 0,\n"
                + "  \"safetyNote\": \"safety note in English or null\",\n"
                + "  \"confidence\": \"HIGH|MEDIUM|LOW\"\n"
                + "}"
        ).formatted(safeWeatherCtx, safeEonetCtx, safeSiteCtx, locationCtx.replace("%", "%%"), safePacksJson, safeUserQuery);

        String raw = callGroq(prompt);
        return parsePackAdvisor(raw, packs);

        } catch (Exception e) {
            log.error("advisePacks failed: {}", e.getMessage(), e);
            return AiPackAdvisorResponseDTO.builder()
                    .offTopic(false)
                    .recommendation("The AI service is temporarily unavailable due to a technical error: " + e.getMessage())
                    .suggestedPackIds(List.of())
                    .suggestedPackNames(List.of())
                    .confidence("LOW")
                    .build();
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515 * 1.609344; // to Kilometers
        return dist;
    }

    // ─────────────────────────────────────────────────────────────────
    //  FEATURE 3 — AI Candidate Compatibility
    // ─────────────────────────────────────────────────────────────────

    public AiCandidatureResponseDTO evaluateCandidateMatching(String participantName, String participantBio, 
                                                                String serviceName, String serviceDesc,
                                                                String eventTitle, String eventDesc) {
        String prompt = """
                Candidate: %s (Bio: %s)
                Service to provide: %s (%s)
                Event Context: %s (%s)
                
                SCORING RULES:
                - Be extremely varied and precise with the score (0-100). 
                - Do not default to 50, 75 or 77. Evaluate the actual keywords and motivation quality.
                - High score (>85) only if the candidate has specific relevant skills or strong motivation for this specific role.
                - Low score (<40) if the letter is generic or lacks effort.
                
                Respond ONLY with valid JSON (no markdown):
                {
                  "score": 0-100,
                  "compatibilitySummary": "2-3 sentences in English explaining the specific match",
                  "strengths": ["Specific Strength 1", "Specific Strength 2"],
                  "risks": ["Specific Risk or null"],
                  "recommendation": "HIRE|INTERVIEW|REJECT"
                }
                """.formatted(participantName, participantBio != null ? participantBio : "No bio provided", 
                               serviceName, serviceDesc, eventTitle, eventDesc);

        try {
            String raw = callGroq(prompt);
            JsonNode node = objectMapper.readTree(extractJson(raw));
            return AiCandidatureResponseDTO.builder()
                    .score(node.path("score").asInt(50))
                    .summary(node.path("compatibilitySummary").asText("No summary available."))
                    .strengths(parseStringList(node.path("strengths")))
                    .risks(parseStringList(node.path("risks")))
                    .recommendation(node.path("recommendation").asText("INTERVIEW"))
                    .build();
        } catch (Exception e) {
            log.error("AI Candidate Matching failed", e);
            return AiCandidatureResponseDTO.builder().score(0).summary("AI analysis unavailable.").build();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  FEATURE 4 — AI Pack Assistant
    // ─────────────────────────────────────────────────────────────────

    public String generatePackDescription(String packName, String services, String siteName) {
        String prompt = String.format(
            "Act as a professional tourism marketing expert for a luxury Tunisian camping brand. " +
            "Generate an attractive, cinematic, and persuasive description (max 3 sentences) for a new camping pack.\\n" +
            "Pack Name: %s\\n" +
            "Included Services: %s\\n" +
            "Location: %s\\n\\n" +
            "The description must be in English, professional, and highlight why this pack is an essential choice for an unforgettable adventure. " +
            "Do not use quotes or introductory text. Just the description.",
            packName, services, siteName
        );

        return callGroq(prompt);
    }

    // ─────────────────────────────────────────────────────────────────
    //  FEATURE 5 — AI reputation Insight
    // ─────────────────────────────────────────────────────────────────

    public String analyzeServiceReputationById(Long serviceId) {
        CampingService service = campingServiceRepository.findById(serviceId).orElse(null);
        if (service == null) return "Service not found.";
        
        List<String> reviews = serviceReviewRepository.findAllCommentsByServiceId(serviceId);
        if (reviews.isEmpty()) return "Not enough data for AI analysis yet.";
        
        String allReviews = String.join("\\n- ", reviews);
        String prompt = String.format(
            "Act as a data analyst. Analyze these customer reviews for the service '%s':\\n\\n%s\\n\\n" +
            "Provide a summary in 3 bullet points:\\n" +
            "1. Overall Sentiment (0-100%%)\\n" +
            "2. Top Strength (What people love most)\\n" +
            "3. Main Area for Improvement.\\n" +
            "Be concise and professional. Use English.",
            service.getName(), allReviews
        );

        return callGroq(prompt);
    }

    public String analyzePackReputationById(Long packId) {
        tn.esprit.projetintegre.entities.Pack pack = packRepository.findById(packId).orElse(null);
        if (pack == null) return "Pack not found.";
        
        StringBuilder allContext = new StringBuilder();
        for (CampingService s : pack.getServices()) {
            List<String> reviews = serviceReviewRepository.findAllCommentsByServiceId(s.getId());
            if (!reviews.isEmpty()) {
                allContext.append("Service: ").append(s.getName()).append("\\n");
                for (String r : reviews) {
                    allContext.append("- ").append(r).append("\\n");
                }
            }
        }

        if (allContext.length() == 0) return "Not enough data for bundle analysis yet.";

        String prompt = String.format(
            "Act as a quality auditor. Analyze the following services and their reviews that belong to the pack '%s':\\n\\n%s\\n\\n" +
            "Provide a summary of the WHOLE pack reputation in 3 bullet points:\\n" +
            "1. Overall Bundle Trust Index (0-100%%)\\n" +
            "2. Strongest Service in the pack\\n" +
            "3. Global Recommendation for the customer.\\n" +
            "Be professional and use English.",
            pack.getName(), allContext.toString()
        );

        return callGroq(prompt);
    }

    public boolean detectCandidatureFraud(String name, String bio) {
        if (bio == null || bio.length() < 10) return true; // Suspicious if too short
        
        String prompt = String.format(
            "Analyze if this job application motivation letter looks like low-effort spam, gibberish, copy-pasted nonsense, or dangerous content.\\n" +
            "Candidate: %s\\nLetter: %s\\n\\n" +
            "CRITERIA FOR SPAM:\\n" +
            "- Repeated words/gibberish (e.g. 'asdf asdf asdf' or 'hello hello hello')\\n" +
            "- Text completely unrelated to camping/volunteering/work\\n" +
            "- One-word answers or random characters\\n\\n" +
            "CRITERIA FOR SAFE:\\n" +
            "- Even short sentences like 'I want to help with the campfire' are SAFE.\\n" +
            "- Expressed interest in the specific event or service.\\n\\n" +
            "Return ONLY the word 'SPAM' if it is highly suspicious/nonsense, or 'SAFE' if it looks like a legitimate human attempt.",
            name, bio
        );

        try {
            String result = callGroq(prompt);
            return result.contains("SPAM");
        } catch (Exception e) {
            log.error("AI Fraud Detection failed", e);
            return false;
        }
    }

    private String callGroq(String userMessage) {
        System.out.println("DEBUG: Calling Groq API with model: " + model);
        if (apiKey == null || apiKey.equals("YOUR_GROQ_API_KEY")) {
            System.err.println("ERROR: Groq API Key is NOT SET or is DEFAULT!");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", 1024);
        body.put("messages", List.of(
                Map.of("role", "system", "content", "You are a helpful AI assistant for a camping resort. Always respond in English."),
                Map.of("role", "user", "content", userMessage)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, JsonNode.class);
            JsonNode choices = Objects.requireNonNull(response.getBody()).get("choices");
            if (choices != null && choices.isArray() && !choices.isEmpty()) {
                String result = choices.get(0).get("message").get("content").asText();
                System.out.println("DEBUG: Groq API Response length: " + result.length());
                return result;
            } else {
                System.err.println("ERROR: Groq API returned empty choices");
            }
        } catch (Exception e) {
            System.err.println("ERROR: Groq API call failed: " + e.getMessage());
            log.error("Groq API call failed: {}", e.getMessage());
        }
        return "{}";
    }

    private AiSitrepResponseDTO parseSitrep(String raw, int alertCount) {
        try {
            JsonNode node = objectMapper.readTree(extractJson(raw));
            return AiSitrepResponseDTO.builder()
                    .threatLevel(node.path("threatLevel").asText("WATCH"))
                    .summary(node.path("summary").asText("Report generated with partial data."))
                    .priorities(parseStringList(node.path("priorities")))
                    .escalationRisk(node.path("escalationRisk").asInt(50))
                    .duplicateWarning(nullableText(node, "duplicateWarning"))
                    .recommendedProtocol(nullableText(node, "recommendedProtocol"))
                    .alertCount(alertCount)
                    .build();
        } catch (Exception e) {
            return AiSitrepResponseDTO.builder().threatLevel("WATCH").summary("Analysis unavailable.").alertCount(alertCount).build();
        }
    }

    private AiPackAdvisorResponseDTO parsePackAdvisor(String raw, List<PackQualityDTO> allPacks) {
        System.out.println("DEBUG: AI Raw Response to parse: " + raw);
        try {
            JsonNode node = objectMapper.readTree(extractJson(raw));
            List<Long> ids = new ArrayList<>();
            if (node.has("suggestedPackIds") && node.get("suggestedPackIds").isArray()) {
                for (JsonNode id : node.get("suggestedPackIds")) ids.add(id.asLong());
            }
            Map<Long, String> nameMap = allPacks.stream().collect(Collectors.toMap(PackQualityDTO::getPackId, PackQualityDTO::getPackName));
            return AiPackAdvisorResponseDTO.builder()
                    .offTopic(node.path("offTopic").asBoolean(false))
                    .recommendation(node.path("recommendation").asText("No recommendation available."))
                    .suggestedPackIds(ids)
                    .suggestedPackNames(ids.stream().map(id -> nameMap.getOrDefault(id, "Pack #" + id)).collect(Collectors.toList()))
                    .totalEstimatedCost(node.path("totalEstimatedCost").asDouble(0))
                    .savingsEstimate(node.path("savingsEstimate").asDouble(0))
                    .safetyNote(nullableText(node, "safetyNote"))
                    .confidence(node.path("confidence").asText("MEDIUM"))
                    .build();
        } catch (Exception e) {
            return AiPackAdvisorResponseDTO.builder().recommendation("Error parsing AI response.").confidence("LOW").build();
        }
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        return (start != -1 && end > start) ? text.substring(start, end + 1) : text;
    }

    private List<String> parseStringList(JsonNode node) {
        List<String> result = new ArrayList<>();
        if (node.isArray()) node.forEach(n -> result.add(n.asText()));
        return result;
    }

    private String nullableText(JsonNode node, String field) {
        JsonNode val = node.path(field);
        return (val.isNull() || val.isMissingNode()) ? null : val.asText();
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception e) { return "[]"; }
    }
}
