package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_reviews", indexes = {
    @Index(name = "idx_service_review_service", columnList = "service_id"),
    @Index(name = "idx_service_review_user", columnList = "user_id"),
    @Index(name = "idx_service_review_rating", columnList = "rating")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"service_id", "user_id"}, name = "uk_service_user_review")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ServiceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note doit être au moins 1")
    @Max(value = 5, message = "La note ne peut pas dépasser 5")
    @Column(nullable = false)
    private Integer rating;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 200, message = "Le titre doit contenir entre 3 et 200 caractères")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Le commentaire est obligatoire")
    @Size(min = 10, max = 2000, message = "Le commentaire doit contenir entre 10 et 2000 caractères")
    @Column(nullable = false, length = 2000)
    private String comment;

    @Min(value = 1, message = "La note de qualité doit être au moins 1")
    @Max(value = 5, message = "La note de qualité ne peut pas dépasser 5")
    private Integer qualityRating;

    @Min(value = 1, message = "La note de rapport qualité-prix doit être au moins 1")
    @Max(value = 5, message = "La note de rapport qualité-prix ne peut pas dépasser 5")
    private Integer valueRating;

    @Min(value = 1, message = "La note de communication doit être au moins 1")
    @Max(value = 5, message = "La note de communication ne peut pas dépasser 5")
    private Integer communicationRating;

    @ElementCollection
    @CollectionTable(name = "service_review_images", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_url", length = 500)
    private List<String> images = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "service_review_pros", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "pro", length = 255)
    private List<String> pros = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "service_review_cons", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "con", length = 255)
    private List<String> cons = new ArrayList<>();

    private Boolean isVerifiedPurchase = false;
    private Boolean isApproved = false;
    private Boolean isHelpful = false;

    @Min(value = 0, message = "Le nombre de likes doit être positif")
    private Integer helpfulCount = 0;

    @Min(value = 0, message = "Le nombre de signalements doit être positif")
    private Integer reportCount = 0;

    @Size(max = 1000, message = "La réponse ne peut pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String providerResponse;

    private LocalDateTime providerResponseAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    @NotNull(message = "Le service est obligatoire")
    @JsonIgnoreProperties({"provider", "site", "images", "rating", "reviewCount"})
    private CampingService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "L'utilisateur est obligatoire")
    @JsonIgnoreProperties({"password", "reviews", "orders", "transactions", "cart", "wallet", "earnedBadges"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

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
