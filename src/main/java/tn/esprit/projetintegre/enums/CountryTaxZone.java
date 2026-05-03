package tn.esprit.projetintegre.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CountryTaxZone {
    EU_FRANCE("FR", 20.0, "France (UE)"),
    EU_GERMANY("DE", 19.0, "Allemagne (UE)"),
    EU_SPAIN("ES", 21.0, "Espagne (UE)"),
    EU_ITALY("IT", 22.0, "Italie (UE)"),
    EU_BELGIUM("BE", 21.0, "Belgique (UE)"),
    EU_LUXEMBOURG("LU", 17.0, "Luxembourg (UE)"),
    EU_NETHERLANDS("NL", 21.0, "Pays-Bas (UE)"),
    EU_OTHER("EU_OTHER", 0.0, "Autre pays UE"),
    TN_TUNISIA("TN", 19.0, "Tunisie"),
    US_USA("US", 0.0, "États-Unis"),
    CA_CANADA("CA", 5.0, "Canada"),
    UK_UK("GB", 20.0, "Royaume-Uni"),
    ASIA_CN("CN", 13.0, "Chine"),
    ASIA_JP("JP", 10.0, "Japon"),
    ASIA_AE("AE", 5.0, "Émirats Arabes Unis"),
    OTHER("OTHER", 0.0, "Autre");

    private final String countryCode;
    private final double standardVatRate;
    private final String label;

    public static CountryTaxZone fromCountryCode(String countryCode) {
        if (countryCode == null) return OTHER;
        String code = countryCode.toUpperCase();
        for (CountryTaxZone zone : values()) {
            if (zone.countryCode.equals(code)) return zone;
        }
        return OTHER;
    }

    public boolean isEU() {
        return this.name().startsWith("EU_");
    }
}