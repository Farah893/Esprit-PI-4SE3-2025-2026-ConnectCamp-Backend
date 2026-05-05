package tn.esprit.projetintegre.dto.response;

import lombok.*;
import tn.esprit.projetintegre.enums.SponsorStatus;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSponsorRequestResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String username;
    private SponsorStatus sponsorStatus;
    private LocalDateTime createdAt;
}
