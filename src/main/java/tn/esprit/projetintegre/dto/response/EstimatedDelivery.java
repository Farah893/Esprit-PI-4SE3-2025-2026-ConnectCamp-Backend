package tn.esprit.projetintegre.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimatedDelivery {
    private LocalDate minDate;
    private LocalDate maxDate;
    private Boolean isExpired;
    private Integer daysRemaining;
}