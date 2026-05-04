package tn.esprit.projetintegre.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.projetintegre.enums.PackType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO interne utilisé par la requête JPQL du Bundle Optimizer.
 * Non exposé directement par l'API — encapsulé dans BundleOptimizerResultDTO.
 */
@Data
@NoArgsConstructor
public class PackOptimizerDTO {

    private Long      packId;
    private String    packName;
    private PackType  packType;
    private BigDecimal packPrice;
    private String    siteName;
    private Long      serviceCount;
    private BigDecimal totalServicesValue;
    private Integer   maxPersons;

    /** Constructeur utilisé par le JPQL new() */
    public PackOptimizerDTO(Long packId, String packName, PackType packType,
                             BigDecimal packPrice, String siteName,
                             Long serviceCount, BigDecimal totalServicesValue,
                             Integer maxPersons) {
        this.packId             = packId;
        this.packName           = packName;
        this.packType           = packType;
        this.packPrice          = packPrice;
        this.siteName           = siteName;
        this.serviceCount       = serviceCount;
        this.totalServicesValue = totalServicesValue;
        this.maxPersons         = maxPersons;
    }

    /**
     * Indice de valeur calculé à la volée.
     * ((totalServicesValue - packPrice) / packPrice) × 100
     */
    public double getValueScore() {
        if (packPrice == null || packPrice.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        if (totalServicesValue == null) return -100.0;
        return totalServicesValue.subtract(packPrice)
                .divide(packPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    public BigDecimal getSavingsAmount() {
        if (totalServicesValue == null || packPrice == null) return BigDecimal.ZERO;
        return totalServicesValue.subtract(packPrice);
    }
}
