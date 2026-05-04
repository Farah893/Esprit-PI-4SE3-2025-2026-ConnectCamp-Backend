package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import tn.esprit.projetintegre.exception.AccessDeniedException;
import tn.esprit.projetintegre.security.SecurityUtil;
import tn.esprit.projetintegre.enums.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.CandidatureService;
import tn.esprit.projetintegre.entities.EventServiceEntity;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.enums.StatutCandidature;
import tn.esprit.projetintegre.exception.BusinessException;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.CandidatureServiceRepository;
import tn.esprit.projetintegre.repositories.EventServiceEntityRepository;
import tn.esprit.projetintegre.repositories.UserRepository;
import tn.esprit.projetintegre.dto.AiCandidatureResponseDTO;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CandidatureServiceLogic {

    private final CandidatureServiceRepository candidatureRepository;
    private final EventServiceEntityRepository eventServiceEntityRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    public CandidatureService applyForService(Long userId, Long eventServiceId, CandidatureService candidatureDetails) {
        if (!SecurityUtil.hasRole(Role.PARTICIPANT) && !SecurityUtil.hasRole(Role.CAMPER)) {
            throw new AccessDeniedException("Only PARTICIPANT or CAMPER can apply for services");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + userId));

        EventServiceEntity eventService = eventServiceEntityRepository.findById(eventServiceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Affectation service-événement non trouvée avec l'ID: " + eventServiceId));

        if (candidatureRepository.existsByCandidatIdAndEventServiceId(userId, eventServiceId)) {
            throw new BusinessException("Vous avez déjà postulé pour ce service dans cet événement");
        }

        // Validate available spots (at least one spot should be available for
        // application to make sense,
        // though technical limit is checked at the 'ACCEPTE' step)
        if (eventService.getQuantiteAcceptee() >= eventService.getQuantiteRequise()) {
            throw new BusinessException("Désolé, toutes les places pour ce service ont déjà été comblées.");
        }

        candidatureDetails.setCandidat(user);
        candidatureDetails.setEventService(eventService);
        candidatureDetails.setService(eventService.getService()); // Populate service_id column
        candidatureDetails.setStatut(StatutCandidature.EN_ATTENTE);

        // --- AI ANALYTICS ---
        try {
            // 1. Detect Fraud / Suspicious Content
            boolean isFraud = aiService.detectCandidatureFraud(user.getName(), candidatureDetails.getLettreMotivation());
            candidatureDetails.setIsSuspicious(isFraud);

            // 2. Evaluate Matching
            String serviceDesc = eventService.getDescription() != null ? eventService.getDescription() : "";
            String eventTitle = eventService.getEvent().getTitle();
            String eventDesc = eventService.getEvent().getDescription() != null ? eventService.getEvent().getDescription() : "";

            AiCandidatureResponseDTO aiResult = aiService.evaluateCandidateMatching(
                user.getName(), 
                candidatureDetails.getLettreMotivation(),
                eventService.getName(),
                serviceDesc,
                eventTitle,
                eventDesc
            );

            candidatureDetails.setScoreEvaluation(aiResult.getScore());
            candidatureDetails.setAiCompatibilitySummary(aiResult.getSummary());
            candidatureDetails.setAiRecommendation(aiResult.getRecommendation());
            candidatureDetails.setAiStrengths(aiResult.getStrengths());
            candidatureDetails.setAiRisks(aiResult.getRisks());

        } catch (Exception e) {
            System.err.println("[AI ERROR] Failed to evaluate candidature: " + e.getMessage());
            candidatureDetails.setScoreEvaluation(0);
            candidatureDetails.setAiRecommendation("PENDING");
        }

        System.out.println("[DEBUG] Saving candidature: " + candidatureDetails.getLettreMotivation());
        return candidatureRepository.save(candidatureDetails);
    }

    public CandidatureService updateCandidatureStatus(Long candidatureId, Long organisateurId,
            StatutCandidature newStatus, String notes) {
        CandidatureService candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Candidature non trouvée avec l'ID: " + candidatureId));

        if (!SecurityUtil.hasRole(Role.ORGANIZER)) {
            throw new AccessDeniedException("Only ORGANIZER can update candidature status");
        }
        User organisateur = userRepository.findById(organisateurId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Organisateur non trouvé avec l'ID: " + organisateurId));

        // Since the relationship is direct now, we just get it from the candidature
        EventServiceEntity eventService = candidature.getEventService();

        if (eventService != null) {
            if (eventService.getEvent().getOrganizer() == null
                    || !eventService.getEvent().getOrganizer().getUser().getId().equals(organisateurId)) {
                throw new BusinessException("Seul l'organisateur de l'événement peut modifier cette candidature");
            }
        }

        StatutCandidature oldStatus = candidature.getStatut();

        // Handle spots increment / decrement
        if (eventService != null) {
            if (newStatus == StatutCandidature.ACCEPTEE && oldStatus != StatutCandidature.ACCEPTEE) {
                if (eventService.getQuantiteAcceptee() >= eventService.getQuantiteRequise()) {
                    throw new BusinessException("Le nombre de places requises pour ce service est déjà atteint.");
                }
                eventService.setQuantiteAcceptee(eventService.getQuantiteAcceptee() + 1);
                eventServiceEntityRepository.save(eventService);
            } else if (newStatus == StatutCandidature.REJETEE && oldStatus == StatutCandidature.ACCEPTEE) {
                eventService.setQuantiteAcceptee(Math.max(0, eventService.getQuantiteAcceptee() - 1));
                eventServiceEntityRepository.save(eventService);
            }
        }

        candidature.setStatut(newStatus);
        candidature.setNotesEvaluation(notes);
        candidature.setEvaluateur(organisateur);
        candidature.setDateDecision(LocalDateTime.now());

        return candidatureRepository.save(candidature);
    }

    public void withdrawCandidature(Long candidatureId, Long userId) {
        if (!SecurityUtil.hasAnyRole(Role.PARTICIPANT, Role.CAMPER)) {
            throw new AccessDeniedException("Only PARTICIPANT or CAMPER can withdraw candidature");
        }
        CandidatureService candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Candidature non trouvée avec l'ID: " + candidatureId));

        if (!candidature.getCandidat().getId().equals(userId)) {
            throw new BusinessException("Vous ne pouvez retirer que vos propres candidatures");
        }

        if (candidature.getStatut() != StatutCandidature.EN_ATTENTE) {
            throw new BusinessException("Vous ne pouvez retirer qu'une candidature en attente");
        }

        candidature.setStatut(StatutCandidature.RETIREE);
        candidatureRepository.save(candidature);
    }

    public java.util.List<CandidatureService> getCandidaturesByEvent(Long eventId) {
        return candidatureRepository.findByEventServiceEventId(eventId);
    }

    public java.util.List<CandidatureService> getCandidaturesByUser(Long userId) {
        return candidatureRepository.findByCandidatId(userId);
    }

    public java.util.List<CandidatureService> getCandidaturesByOrganizer(Long organizerUserId) {
        return candidatureRepository.findByEventService_Event_Organizer_User_Id(organizerUserId);
    }
}
