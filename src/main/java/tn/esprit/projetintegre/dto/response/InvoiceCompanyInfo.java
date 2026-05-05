package tn.esprit.projetintegre.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceCompanyInfo {
    private String companyName;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private String email;
    private String phone;
    private String website;
    private String siret;
    private String tvaNumber;
    private String logoUrl;
}
