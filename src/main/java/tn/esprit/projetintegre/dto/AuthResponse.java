package tn.esprit.projetintegre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.projetintegre.enums.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String name;
    private Role role;
    private Long organizerId;
    private Boolean isSeller;
    private Boolean isBuyer;
    private String storeName;
}
