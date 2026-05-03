package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tn.esprit.projetintegre.enums.ServiceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "camping_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CampingService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Service name is required")
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Service type is required")
    private ServiceType type;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    private String pricingUnit; // PER_HOUR, PER_DAY, PER_PERSON

    @ManyToOne
    @JoinColumn(name = "provider_id")
    @JsonIgnoreProperties({"password", "reviews", "orders", "transactions", "cart", "wallet", "earnedBadges"})
    private User provider;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;

    @ElementCollection(fetch = jakarta.persistence.FetchType.EAGER)
    @CollectionTable(name = "service_images", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Builder.Default
    private Boolean isActive = true;
    @Builder.Default
    private Boolean isAvailable = true;
    // New flags to differentiate service visibility
    @Builder.Default
    private Boolean isCamperOnly = false;
    @Builder.Default
    private Boolean isOrganizerService = false;

    private Integer maxCapacity;
    private Integer duration; // in minutes

    @Column(precision = 3, scale = 2)
    private BigDecimal rating;
    @Builder.Default
    private Integer reviewCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
