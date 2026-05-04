package tn.esprit.projetintegre.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente un événement naturel retourné par l'API NASA EONET v3.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EonetEventDTO {
    private String id;
    private String title;
    private String category;
    private double latitude;
    private double longitude;
}
