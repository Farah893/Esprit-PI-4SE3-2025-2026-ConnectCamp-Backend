package tn.esprit.projetintegre.dto.response;

import lombok.*;
import tn.esprit.projetintegre.enums.TrackingStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentTrackingStatus {
    private TrackingStatus code;
    private String label;
    private String severity;
    private LocalDateTime timestamp;
    private String location;
    private String description;
}
