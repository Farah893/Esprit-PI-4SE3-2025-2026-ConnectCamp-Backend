package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.CampingService;
import tn.esprit.projetintegre.entities.Site;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.enums.ServiceType;
import tn.esprit.projetintegre.exception.CannotDeleteServiceException;
import tn.esprit.projetintegre.exception.AccessDeniedException;
import tn.esprit.projetintegre.security.SecurityUtil;
import tn.esprit.projetintegre.enums.Role;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.CampingServiceRepository;
import tn.esprit.projetintegre.repositories.EventServiceEntityRepository;
import tn.esprit.projetintegre.repositories.SiteRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CampingServiceService {

    private final CampingServiceRepository campingServiceRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final EventServiceEntityRepository eventServiceEntityRepository;

    public Page<CampingService> getAllServices(Pageable pageable) {
        return campingServiceRepository.findAll(pageable);
    }

    public CampingService getServiceById(Long id) {
        return campingServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camping service not found with id: " + id));
    }

    public Page<CampingService> getActiveServices(Pageable pageable) {
        return campingServiceRepository.findByIsActiveTrue(pageable);
    }

    public Page<CampingService> getOrganizerServices(Pageable pageable) {
        return campingServiceRepository.findByIsActiveTrueAndIsOrganizerServiceTrue(pageable);
    }

    public List<CampingService> getServicesByType(ServiceType type) {
        return campingServiceRepository.findByType(type);
    }

    public Page<CampingService> getServicesBySiteId(Long siteId, Pageable pageable) {
        return campingServiceRepository.findBySiteId(siteId, pageable);
    }

    public Page<CampingService> getServicesByProviderId(Long providerId, Pageable pageable) {
        return campingServiceRepository.findByProviderId(providerId, pageable);
    }

    public CampingService createService(CampingService service, Long providerId, Long siteId) {
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with id: " + providerId));
        
        // Permettre à l'ADMIN ou au prestataire de créer des services
        if (!SecurityUtil.hasRole(Role.ADMIN) && !SecurityUtil.hasRole(Role.SELLER)) {
            throw new AccessDeniedException("Only ADMIN or SELLER can create services");
        }
        
        service.setProvider(provider);

        if (siteId != null) {
            Site site = siteRepository.findById(siteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
            service.setSite(site);
        }

        service.setIsActive(true);
        service.setIsAvailable(service.getIsAvailable() != null ? service.getIsAvailable() : true);
        service.setIsCamperOnly(service.getIsCamperOnly() != null ? service.getIsCamperOnly() : false);
        service.setIsOrganizerService(
                service.getIsOrganizerService() != null ? service.getIsOrganizerService() : false);
        return campingServiceRepository.save(service);
    }

    public CampingService updateService(Long id, CampingService serviceDetails) {
        CampingService service = getServiceById(id);
        
        // Vérification de sécurité : ADMIN ou Propriétaire du service
        if (!SecurityUtil.hasRole(Role.ADMIN) && !service.getProvider().getEmail().equals(SecurityUtil.getCurrentUserEmail())) {
            throw new AccessDeniedException("You don't have permission to update this service");
        }
        
        service.setName(serviceDetails.getName());
        service.setDescription(serviceDetails.getDescription());
        service.setType(serviceDetails.getType());
        service.setPrice(serviceDetails.getPrice());
        service.setPricingUnit(serviceDetails.getPricingUnit());
        service.setImages(serviceDetails.getImages());
        service.setMaxCapacity(serviceDetails.getMaxCapacity());
        service.setDuration(serviceDetails.getDuration());
        if (serviceDetails.getIsAvailable() != null) {
            service.setIsAvailable(serviceDetails.getIsAvailable());
        }
        if (serviceDetails.getIsActive() != null) {
            service.setIsActive(serviceDetails.getIsActive());
        }
        return campingServiceRepository.save(service);
    }

    public void deleteService(Long id) {
        CampingService service = getServiceById(id);
        
        if (!SecurityUtil.hasRole(Role.ADMIN) && !service.getProvider().getEmail().equals(SecurityUtil.getCurrentUserEmail())) {
            throw new AccessDeniedException("You don't have permission to delete this service");
        }
        
        if (eventServiceEntityRepository.existsByServiceId(id)) {
            throw new CannotDeleteServiceException(
                    "Cannot delete service because it is assigned to one or more events.");
        }

        service.setIsActive(false);
        campingServiceRepository.save(service);
    }

    public List<Object[]> getAtRiskServices() {
        return campingServiceRepository.findAtRiskServices();
    }
}
