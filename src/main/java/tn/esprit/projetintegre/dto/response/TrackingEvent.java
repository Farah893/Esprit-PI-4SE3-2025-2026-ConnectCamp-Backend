package tn.esprit.projetintegre.dto.response;

import lombok.*;
import tn.esprit.projetintegre.enums.TrackingStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEvent {
    private LocalDateTime timestamp;
    private TrackingStatus status;
    private String location;
    private String description;
    private String carrierStatus;
}
