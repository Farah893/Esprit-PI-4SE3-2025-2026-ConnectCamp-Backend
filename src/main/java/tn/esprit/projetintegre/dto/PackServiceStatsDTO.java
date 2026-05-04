package tn.esprit.projetintegre.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.projetintegre.enums.PackType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO résultat de la requête JPQL avec jointures :
 * Pack ⟶ Site, CampingService (ManyToMany via pack_services)
 * Retourne les packs actifs enrichis du nom du site,
 * du nombre de services inclus, de la valeur totale des services
 * et du pourcentage de réduction calculé.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackServiceStatsDTO {

    private Long packId;
    private String packName;
    private PackType packType;
    private BigDecimal packPrice;
    private BigDecimal originalPrice;
    private String siteName;
    private Long serviceCount;
    private BigDecimal totalServicesValue;

    /**
     * Pourcentage de réduction calculé dynamiquement
     * à partir de originalPrice et packPrice.
     */
    public Double getDiscountPercentage() {
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0 && packPrice != null) {
            return originalPrice.subtract(packPrice)
                    .divide(originalPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
        return 0.0;
    }
}
