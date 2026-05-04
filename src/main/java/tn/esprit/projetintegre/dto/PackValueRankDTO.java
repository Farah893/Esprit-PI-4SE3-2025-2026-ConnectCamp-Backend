package tn.esprit.projetintegre.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.projetintegre.enums.PackType;

import java.math.BigDecimal;

/**
 * DTO résultat de l'API Value Ranking.
 *
 * Classe les packs actifs par "indice de valeur" décroissant :
 *   valueScore = ((valeurTotaleServices - prixPack) / prixPack) × 100
 *
 * Exemple : pack à 150 TND dont les services valent 220 TND
 *   → valueScore = ((220 - 150) / 150) × 100 = 46.67 %
 *   → Le client économise 46.67% en achetant ce pack plutôt qu'à la carte.
 *
 * Seuls les packs où totalServicesValue > packPrice sont inclus
 * (les packs sans économie réelle sont exclus du classement).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackValueRankDTO {

    /** Position dans le classement (1 = meilleur deal). */
    private Integer rank;

    private Long      packId;
    private String    packName;
    private PackType  packType;
    private String    siteName;

    /** Prix facturé au client. */
    private BigDecimal packPrice;

    /** Somme des prix individuels des services inclus. */
    private BigDecimal totalServicesValue;

    /** Économie absolue en TND : totalServicesValue - packPrice. */
    private BigDecimal savingsAmount;

    /**
     * Indice de valeur en % :
     * ((totalServicesValue - packPrice) / packPrice) × 100
     */
    private Double valueScore;

    private Long serviceCount;
}
