package tn.esprit.projetintegre.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorshipRequest {

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Sponsorship type is required")  // ← AJOUTÉ (obligatoire dans l'entité)
    private String sponsorshipType;

    private String sponsorshipLevel;

    private String description;

    @NotBlank(message = "Currency is required")  // ← AJOUTÉ (obligatoire dans l'entité)
    private String currency;

    @NotNull(message = "Start date is required")  // ← AJOUTÉ (obligatoire dans l'entité)
    private LocalDate startDate;

    @NotNull(message = "End date is required")    // ← AJOUTÉ (obligatoire dans l'entité)
    private LocalDate endDate;

    private String benefits;
    private String deliverables;
    private String notes;
    private String status;   // ← AJOUTÉ (optionnel, défaut PENDING dans @PrePersist)
    private Boolean isActive;
}
