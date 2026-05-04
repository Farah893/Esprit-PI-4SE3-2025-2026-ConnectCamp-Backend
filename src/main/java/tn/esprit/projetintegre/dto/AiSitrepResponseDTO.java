package tn.esprit.projetintegre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Résultat de l'analyse IA des alertes actives.
 * Généré par Claude Haiku via POST /api/ai/sitrep
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSitrepResponseDTO {

    /** Niveau de menace global : SAFE | WATCH | DANGER | CRITICAL */
    private String threatLevel;

    /** Résumé narratif de la situation (2-3 phrases, en français) */
    private String summary;

    /** Actions prioritaires classées par importance */
    private List<String> priorities;

    /** Risque d'escalade dans les 2 prochaines heures (0-100) */
    private int escalationRisk;

    /** Alertes potentiellement dupliquées détectées, ou null */
    private String duplicateWarning;

    /** Protocole recommandé par l'IA, ou null */
    private String recommendedProtocol;

    /** Nombre d'alertes analysées */
    private int alertCount;
}
