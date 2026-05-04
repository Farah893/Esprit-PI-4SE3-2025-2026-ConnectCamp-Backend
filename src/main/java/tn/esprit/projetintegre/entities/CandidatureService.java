package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.projetintegre.enums.StatutCandidature;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "candidatures_service_v2", indexes = {
        @Index(name = "idx_cand_user", columnList = "user_id"),
        @Index(name = "idx_cand_service", columnList = "camping_service_id"),
        @Index(name = "idx_cand_statut", columnList = "statut")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatureService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50, message = "Le numéro ne peut pas dépasser 50 caractères")
    @Column(unique = true)
    private String numeroCandidature;

    @NotBlank(message = "La lettre de motivation est obligatoire")
    @Size(max = 5000, message = "La lettre ne peut pas dépasser 5000 caractères")
    @Column(length = 5000)
    private String lettreMotivation;

    @Size(max = 2000, message = "L'expérience ne peut pas dépasser 2000 caractères")
    @Column(length = 2000)
    private String experiencePertinente;

    @ElementCollection
    @CollectionTable(name = "candidature_competences", joinColumns = @JoinColumn(name = "candidature_id"))
    @Column(name = "competence")
    @Builder.Default
    private List<String> competences = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutCandidature statut = StatutCandidature.EN_ATTENTE;

    @Size(max = 2000, message = "Les notes ne peuvent pas dépasser 2000 caractères")
    @Column(length = 2000)
    private String notesEvaluation;

    @Min(value = 0, message = "Le score doit être positif")
    @Max(value = 100, message = "Le score ne peut pas dépasser 100")
    private Integer scoreEvaluation;

    @Column(length = 2000)
    private String aiCompatibilitySummary;

    @Column(length = 50)
    private String aiRecommendation;

    @ElementCollection
    @CollectionTable(name = "candidature_ai_strengths", joinColumns = @JoinColumn(name = "candidature_id"))
    @Column(name = "strength")
    private List<String> aiStrengths = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "candidature_ai_risks", joinColumns = @JoinColumn(name = "candidature_id"))
    @Column(name = "risk")
    private List<String> aiRisks = new ArrayList<>();

    private Boolean isSuspicious = false;

    private LocalDateTime dateEntretien;
    private LocalDateTime dateDecision;

    @Size(max = 1000, message = "Le motif de refus ne peut pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String motifRefus;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User candidat;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camping_service_id")
    private CampingService service;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_service_id", nullable = false)
    private EventServiceEntity eventService;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluateur_id")
    private User evaluateur;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (numeroCandidature == null) {
            numeroCandidature = "CAND-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @JsonProperty("eventId")
    public Long fetchEventId() {
        return (eventService != null && eventService.getEvent() != null) ? eventService.getEvent().getId() : null;
    }

    @JsonProperty("serviceName")
    public String fetchServiceName() {
        return (eventService != null && eventService.getName() != null) ? eventService.getName() : null;
    }

    @JsonProperty("nomParticipant")
    public String fetchNomParticipant() {
        return (candidat != null) ? candidat.getName() : "Inconnu";
    }

    @JsonProperty("eventName")
    public String fetchEventName() {
        return (eventService != null && eventService.getEvent() != null) ? eventService.getEvent().getTitle() : "Événement inconnu";
    }

    @JsonProperty("eventImage")
    public String fetchEventImage() {
        if (eventService != null && eventService.getEvent() != null) {
            Event e = eventService.getEvent();
            return (e.getImages() != null && !e.getImages().isEmpty()) ? e.getImages().get(0) : e.getThumbnail();
        }
        return null;
    }

    @JsonProperty("participantAvatar")
    public String fetchParticipantAvatar() {
        return (candidat != null) ? candidat.getAvatar() : null;
    }

    @JsonProperty("participantId")
    public Long fetchParticipantId() {
        return (candidat != null) ? candidat.getId() : null;
    }

    @JsonProperty("candidatId") // Alternative name
    public Long fetchCandidatId() {
        return (candidat != null) ? candidat.getId() : null;
    }

    @JsonProperty("eventServiceId")
    public Long fetchEventServiceId() {
        return (eventService != null) ? eventService.getId() : null;
    }

    @JsonProperty("serviceId")
    public Long fetchServiceId() {
        return (service != null) ? service.getId() : null;
    }

    @JsonProperty("participantName")
    public String fetchParticipantName() {
        return (candidat != null) ? candidat.getName() : "Inconnu";
    }

    @JsonProperty("candidatName") // Used by CandidatureManageComponent
    public String fetchCandidatName() {
        return (candidat != null) ? candidat.getName() : "Inconnu";
    }

    @JsonProperty("eventServiceName") // Used by CandidatureManageComponent
    public String fetchEventServiceName() {
        return (eventService != null) ? eventService.getName() : "N/A";
    }
}
