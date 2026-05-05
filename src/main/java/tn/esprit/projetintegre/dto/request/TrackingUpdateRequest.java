package tn.esprit.projetintegre.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.esprit.projetintegre.enums.TrackingStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingUpdateRequest {

    @NotNull(message = "Le statut de tracking est obligatoire")
    private TrackingStatus status;

    private String location;
    private String description;

    /** Statut brut retourné par le transporteur (pour usage futur V2) */
    private String carrierStatus;
}
