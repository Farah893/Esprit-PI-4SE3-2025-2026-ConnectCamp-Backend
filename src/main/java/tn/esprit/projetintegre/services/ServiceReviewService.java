package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.CampingService;
import tn.esprit.projetintegre.entities.ServiceReview;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.CampingServiceRepository;
import tn.esprit.projetintegre.repositories.ServiceReviewRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceReviewService {

    private final ServiceReviewRepository serviceReviewRepository;
    private final CampingServiceRepository campingServiceRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    public Page<ServiceReview> getReviewsByService(Long serviceId, Pageable pageable) {
        // Si l'utilisateur est Admin, on renvoie tout (pour résolution)
        if (tn.esprit.projetintegre.security.SecurityUtil.hasRole(tn.esprit.projetintegre.enums.Role.ADMIN)) {
            return serviceReviewRepository.findByServiceId(serviceId, pageable);
        }
        // Sinon, seulement les approuvés
        return serviceReviewRepository.findByServiceIdAndIsApprovedTrue(serviceId, pageable);
    }

    public ServiceReview createReview(ServiceReview review, Long serviceId, Long userId) {
        CampingService service = campingServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // --- Review Authenticity Guard (AI Validator) ---
        tn.esprit.projetintegre.dto.ReviewAnalysisResultDTO analysis = aiService.validateReviewConsistency(review.getRating(), review.getComment());

        // Chercher si un avis existe déjà pour le mettre à jour
        ServiceReview existing = serviceReviewRepository.findByServiceIdAndUserId(serviceId, userId).orElse(null);
        
        if (existing != null) {
            existing.setRating(review.getRating());
            existing.setQualityRating(review.getQualityRating());
            existing.setValueRating(review.getValueRating());
            existing.setTitle(review.getTitle());
            existing.setComment(review.getComment());
            existing.setPros(review.getPros());
            existing.setCons(review.getCons());
            existing.setIsApproved(analysis.isConsistent()); // Déapprouvé si incohérent
            existing.setAiConsistencyAlert(analysis.getReason());
            return serviceReviewRepository.save(existing);
        }

        review.setService(service);
        review.setUser(user);
        review.setIsApproved(analysis.isConsistent()); // Déapprouvé si incohérent
        review.setAiConsistencyAlert(analysis.getReason());
        
        return serviceReviewRepository.save(review);
    }

    public void approveReview(Long id) {
        ServiceReview review = serviceReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setIsApproved(true);
        serviceReviewRepository.save(review);
    }

    public void deleteReview(Long id) {
        serviceReviewRepository.deleteById(id);
    }
}
