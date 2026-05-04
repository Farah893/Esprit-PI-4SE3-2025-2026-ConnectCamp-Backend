package tn.esprit.projetintegre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Réponse du conseiller IA de packs.
 * Retourné par POST /api/ai/pack-advisor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPackAdvisorResponseDTO {

    /** Texte de recommandation personnalisé en français */
    private String recommendation;

    /** IDs des packs suggérés */
    private List<Long> suggestedPackIds;

    /** Noms des packs suggérés (résolus depuis les IDs) */
    private List<String> suggestedPackNames;

    /** Coût total estimé des packs suggérés (TND) */
    private double totalEstimatedCost;

    /** Économie estimée vs. achat à la carte (TND) */
    private double savingsEstimate;

    /** Note de sécurité si des événements naturels ont été détectés, ou null */
    private String safetyNote;

    /** Confiance de l'IA dans la recommandation : HIGH | MEDIUM | LOW */
    private String confidence;

    /** True si la question n'est pas liée aux packs camping */
    @Builder.Default
    private boolean offTopic = false;
}
