package tn.esprit.projetintegre.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import tn.esprit.projetintegre.dto.response.InvoiceCompanyInfo;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "invoice")
public class InvoiceConfiguration {

    private String companyName = "ConnectCamp SARL";
    private String companyAddress = "123 Avenue du Commerce";
    private String companyCity = "Paris";
    private String companyPostalCode = "75001";
    private String companyCountry = "France";
    private String companyEmail = "contact@connectcamp.fr";
    private String companyPhone = "+33 1 23 45 67 89";
    private String companyWebsite = "https://www.connectcamp.fr";
    private String siret = "123 456 789 00012";
    private String tvaNumber = "FR 12 345 678 901";
    private String logoUrl = "/uploads/logo.png";
    private String invoicePrefix = "FAC";
    private Integer invoiceValidityDays = 30;
    private String currency = "EUR";
    private BigDecimal defaultTaxRate = new BigDecimal("20.0");
    private String legalNotice = "Délai de rétractation: 14 jours. Garantie légale de conformité: 2 ans.";
    private String latePaymentPenalty = "Pénalités de retard: 3 fois le taux d'intérêt légal.";
    private String collectionFee = "Indemnité forfaitaire de recouvrement: 40 EUR.";

    public InvoiceCompanyInfo getCompanyInfo() {
        return InvoiceCompanyInfo.builder()
                .companyName(companyName)
                .address(companyAddress)
                .city(companyCity)
                .postalCode(companyPostalCode)
                .country(companyCountry)
                .email(companyEmail)
                .phone(companyPhone)
                .website(companyWebsite)
                .siret(siret)
                .tvaNumber(tvaNumber)
                .logoUrl(logoUrl)
                .build();
    }

    // Getters & Setters
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }

    public String getCompanyCity() { return companyCity; }
    public void setCompanyCity(String companyCity) { this.companyCity = companyCity; }

    public String getCompanyPostalCode() { return companyPostalCode; }
    public void setCompanyPostalCode(String companyPostalCode) { this.companyPostalCode = companyPostalCode; }

    public String getCompanyCountry() { return companyCountry; }
    public void setCompanyCountry(String companyCountry) { this.companyCountry = companyCountry; }

    public String getCompanyEmail() { return companyEmail; }
    public void setCompanyEmail(String companyEmail) { this.companyEmail = companyEmail; }

    public String getCompanyPhone() { return companyPhone; }
    public void setCompanyPhone(String companyPhone) { this.companyPhone = companyPhone; }

    public String getCompanyWebsite() { return companyWebsite; }
    public void setCompanyWebsite(String companyWebsite) { this.companyWebsite = companyWebsite; }

    public String getSiret() { return siret; }
    public void setSiret(String siret) { this.siret = siret; }

    public String getTvaNumber() { return tvaNumber; }
    public void setTvaNumber(String tvaNumber) { this.tvaNumber = tvaNumber; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getInvoicePrefix() { return invoicePrefix; }
    public void setInvoicePrefix(String invoicePrefix) { this.invoicePrefix = invoicePrefix; }

    public Integer getInvoiceValidityDays() { return invoiceValidityDays; }
    public void setInvoiceValidityDays(Integer invoiceValidityDays) { this.invoiceValidityDays = invoiceValidityDays; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getDefaultTaxRate() { return defaultTaxRate; }
    public void setDefaultTaxRate(BigDecimal defaultTaxRate) { this.defaultTaxRate = defaultTaxRate; }

    public String getLegalNotice() { return legalNotice; }
    public void setLegalNotice(String legalNotice) { this.legalNotice = legalNotice; }

    public String getLatePaymentPenalty() { return latePaymentPenalty; }
    public void setLatePaymentPenalty(String latePaymentPenalty) { this.latePaymentPenalty = latePaymentPenalty; }

    public String getCollectionFee() { return collectionFee; }
    public void setCollectionFee(String collectionFee) { this.collectionFee = collectionFee; }
}
