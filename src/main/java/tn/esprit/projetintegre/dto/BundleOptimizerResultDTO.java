package tn.esprit.projetintegre.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO résultat de l'API Bundle Optimizer.
 *
 * Retourne la combinaison optimale de packs pour un budget et un nombre
 * de personnes donnés, sélectionnée par algorithme greedy :
 *
 *   1. Filtrer : packs actifs dont maxPersons >= persons ET price <= budget
 *   2. Trier   : par indice de valeur ((services - prix) / prix × 100) décroissant
 *   3. Sélectionner : ajouter chaque pack tant que le budget le permet
 *
 * L'objectif est de maximiser la valeur totale des services inclus
 * dans la contrainte budget, pas seulement de remplir le budget.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BundleOptimizerResultDTO {

    /** Liste des packs sélectionnés, triés par indice de valeur décroissant. */
    private List<PackOptimizerDTO> selectedPacks;

    /** Somme des prix des packs sélectionnés. */
    private BigDecimal totalPrice;

    /** Valeur totale des services inclus dans les packs sélectionnés. */
    private BigDecimal totalServicesValue;

    /** Économie totale = totalServicesValue - totalPrice. */
    private BigDecimal totalSavings;

    /** Budget initial fourni par l'utilisateur. */
    private BigDecimal budget;

    /** Budget restant après sélection. */
    private BigDecimal remainingBudget;

    /** Nombre de personnes pour lequel l'optimisation a été effectuée. */
    private Integer persons;

    /** Nombre de packs sélectionnés. */
    private Integer packCount;

    /** Message si aucun pack ne correspond aux critères. */
    private String message;
}
