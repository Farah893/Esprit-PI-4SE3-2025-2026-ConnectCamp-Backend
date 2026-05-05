package tn.esprit.projetintegre.services;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class CurrencyService {

    // 🔥 Taux statiques (simple version)
    private static final Map<String, BigDecimal> RATES = Map.of(
            "EUR", BigDecimal.ONE,
            "USD", new BigDecimal("1.08"),
            "TND", new BigDecimal("3.35")
    );

    public BigDecimal convert(BigDecimal amount, String from, String to) {
        if (amount == null) return BigDecimal.ZERO;

        BigDecimal fromRate = RATES.getOrDefault(from, BigDecimal.ONE);
        BigDecimal toRate   = RATES.getOrDefault(to, BigDecimal.ONE);

        return amount
                .divide(fromRate, 6, RoundingMode.HALF_UP)
                .multiply(toRate)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
