// dto/response/CategorySalesReportResponse.java
package tn.esprit.projetintegre.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySalesReportResponse {

    private Long   categoryId;
    private String categoryName;

    private Long       totalOrders;      // commandes distinctes ayant touché cette catégorie
    private Long       totalItemsSold;   // somme des quantités vendues
    private BigDecimal totalRevenue;     // somme(orderItem.totalPrice) des commandes DELIVERED
    private BigDecimal averageOrderValue;// totalRevenue / totalOrders
}