package tn.esprit.projetintegre.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum ShippingZone {
    DOMESTIC(new BigDecimal("1.0"), "Livraison nationale"),
    EU(new BigDecimal("1.5"), "Union Européenne"),
    MAGHREB(new BigDecimal("2.0"), "Maghreb"),
    MIDDLE_EAST(new BigDecimal("2.5"), "Moyen-Orient"),
    NORTH_AMERICA(new BigDecimal("3.0"), "Amérique du Nord"),
    ASIA(new BigDecimal("3.5"), "Asie"),
    OTHER(new BigDecimal("4.0"), "Autre");

    private final BigDecimal multiplier;
    private final String label;

    public static ShippingZone fromCountryCode(String countryCode) {
        if (countryCode == null) return OTHER;
        String code = countryCode.toUpperCase();

        // Domestic (base country is France)
        if ("FR".equals(code)) return DOMESTIC;

        // EU countries (delegate to CountryTaxZone)
        if (CountryTaxZone.fromCountryCode(code).isEU()) return EU;

        // Maghreb
        if ("TN".equals(code) || "MA".equals(code) || "DZ".equals(code) || "LY".equals(code)) return MAGHREB;

        // Middle East
        if ("AE".equals(code) || "SA".equals(code) || "QA".equals(code) || "KW".equals(code) ||
                "BH".equals(code) || "OM".equals(code)) return MIDDLE_EAST;

        // North America
        if ("US".equals(code) || "CA".equals(code) || "MX".equals(code)) return NORTH_AMERICA;

        // Asia
        if ("CN".equals(code) || "JP".equals(code) || "KR".equals(code) || "IN".equals(code) ||
                "SG".equals(code) || "TH".equals(code)) return ASIA;

        return OTHER;
    }
}