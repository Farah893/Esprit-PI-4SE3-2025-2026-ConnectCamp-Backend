package tn.esprit.projetintegre.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsResponse {

    private String status;           // PENDING, DELIVERED, CANCELLED, etc.
    private Long orderCount;         // Nombre de commandes pour ce statut
    private Long totalItemsSold;     // Total articles vendus
    private BigDecimal avgRevenuePerItemType; // Revenu moyen par type d'article
}
