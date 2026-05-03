package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.projetintegre.enums.CountryTaxZone;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TaxBreakdown {
    private CountryTaxZone zone;
    private BigDecimal rate;    // Percentage (e.g., 20.0 for 20%)
    private BigDecimal amount;  // Calculated tax amount in currency units
}