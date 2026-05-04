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
        return serviceReviewRepository.findByServiceIdAndIsApprovedTrue(serviceId, pageable);
    }

    public ServiceReview createReview(ServiceReview review, Long serviceId, Long userId) {
        CampingService service = campingServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // --- Review Authenticity Guard (AI Validator) ---
        boolean isConsistent = aiService.validateReviewConsistency(review.getRating(), review.getComment());

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
            existing.setIsApproved(isConsistent); // Déapprouvé si incohérent
            return serviceReviewRepository.save(existing);
        }

        review.setService(service);
        review.setUser(user);
        review.setIsApproved(isConsistent); // Déapprouvé si incohérent
        
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
