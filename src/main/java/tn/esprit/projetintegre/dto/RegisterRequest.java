package tn.esprit.projetintegre.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.projetintegre.enums.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;
    private String address;
    private String country;
    private Integer age;
    private Role role;

    @JsonProperty("isSeller")
    private Boolean isSeller;

    @JsonProperty("isBuyer")
    private Boolean isBuyer;

    private String storeName;
    private String bio;
}