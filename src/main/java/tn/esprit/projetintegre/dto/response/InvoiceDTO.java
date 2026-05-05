package tn.esprit.projetintegre.dto.response;

import lombok.*;
import tn.esprit.projetintegre.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {

    // Infos facture
    private Long id;
    private String invoiceNumber;
    private String orderNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDateTime paidAt;

    // Émetteur (entreprise)
    private InvoiceCompanyInfo company;

    // Client
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String billingAddress;

    // Lignes de commande
    private List<InvoiceLineItemDTO> items;

    // Montants
    private BigDecimal subtotalHT;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
    private BigDecimal totalShipping;
    private BigDecimal totalTTC;

    // Paiement
    private PaymentStatus paymentStatus;
    private String paymentMethod;

    // Fichiers
    private String pdfUrl;
    private String qrCodeUrl;
    private String verificationUrl;

    // Notes & mentions légales
    private String notes;
    private String legalNotice;

    // Metadata
    private LocalDateTime createdAt;
}
