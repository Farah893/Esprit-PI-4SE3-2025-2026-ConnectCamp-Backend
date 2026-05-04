package tn.esprit.projetintegre.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Requête du conseiller IA de packs.
 * Le frontend envoie la question de l'utilisateur + contexte EONET optionnel.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiPackAdvisorRequestDTO {

    /** Question en langage naturel de l'utilisateur */
    private String userQuery;

    /** Budget en TND (optionnel) */
    private Double budget;

    /** Nombre de personnes (optionnel) */
    private Integer persons;

    /**
     * Latitude GPS de l'utilisateur (optionnel).
     * Si fourni, le backend récupère automatiquement les événements NASA EONET
     * à proximité — le frontend n'a plus besoin d'appeler NASA lui-même.
     */
    private Double latitude;

    /**
     * Longitude GPS de l'utilisateur (optionnel).
     * Utilisé conjointement avec latitude pour l'auto-fetch EONET.
     */
    private Double longitude;

    /**
     * Événements EONET actifs passés manuellement par le frontend (titres).
     * Ignoré si latitude/longitude sont fournis (le backend fait le fetch lui-même).
     */
    private List<String> eonetEvents;

    /** Niveau de risque du site ciblé (ex: "WATCH") — optionnel */
    private String siteRiskLevel;
}
