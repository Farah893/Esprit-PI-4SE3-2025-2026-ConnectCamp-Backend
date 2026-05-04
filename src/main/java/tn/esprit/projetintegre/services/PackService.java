package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.esprit.projetintegre.dto.BundleOptimizerResultDTO;
import tn.esprit.projetintegre.dto.PackOptimizerDTO;
import tn.esprit.projetintegre.dto.PackQualityDTO;
import tn.esprit.projetintegre.dto.PackServiceStatsDTO;
import tn.esprit.projetintegre.dto.PackValueRankDTO;
import tn.esprit.projetintegre.exception.AccessDeniedException;
import tn.esprit.projetintegre.security.SecurityUtil;
import tn.esprit.projetintegre.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.dto.PackDTO;
import tn.esprit.projetintegre.entities.Pack;
import tn.esprit.projetintegre.entities.Site;
import tn.esprit.projetintegre.entities.CampingService;
import tn.esprit.projetintegre.enums.PackType;
import tn.esprit.projetintegre.enums.ServiceType;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.exception.BusinessException;
import tn.esprit.projetintegre.repositories.PackRepository;
import tn.esprit.projetintegre.repositories.SiteRepository;
import tn.esprit.projetintegre.repositories.CampingServiceRepository;
import tn.esprit.projetintegre.repositories.ServiceReviewRepository;
import tn.esprit.projetintegre.entities.ServiceReview;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PackService {

    private final PackRepository packRepository;
    private final SiteRepository siteRepository;
    private final CampingServiceRepository campingServiceRepository;
    private final ServiceReviewRepository serviceReviewRepository;

    private void calculateAndValidatePricing(Pack pack) {

        if (pack.getValidFrom() != null && pack.getValidUntil() != null
                && pack.getValidFrom().isAfter(pack.getValidUntil())) {
            throw new BusinessException("La date de début doit être antérieure à la date de fin de validité.");
        }

        if (pack.getServices() != null && !pack.getServices().isEmpty()) {
            BigDecimal computedOriginalPrice = BigDecimal.ZERO;
            for (CampingService service : pack.getServices()) {
                if (service.getPrice() != null) {
                    computedOriginalPrice = computedOriginalPrice.add(service.getPrice());
                }
            }
            pack.setOriginalPrice(computedOriginalPrice);
        } else {
            pack.setOriginalPrice(pack.getPrice());
        }

        if (pack.getOriginalPrice() != null && pack.getPrice() != null) {
            if (pack.getPrice().compareTo(pack.getOriginalPrice()) >= 0 && pack.getServices() != null
                    && !pack.getServices().isEmpty()) {
                throw new BusinessException("Le prix du pack (" + pack.getPrice()
                        + ") doit être inférieur au prix normal cumulé (" + pack.getOriginalPrice() + ").");
            }

            if (pack.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal economy = pack.getOriginalPrice().subtract(pack.getPrice());
                BigDecimal percentage = economy.divide(pack.getOriginalPrice(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                pack.setDiscountPercentage(percentage.doubleValue());
            } else {
                pack.setDiscountPercentage(0.0);
            }
        }
    }

    public PackDTO.Response createPack(PackDTO.CreateRequest request) {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can create packs");
        }
        Pack pack = Pack.builder()
                .name(request.getName())
                .description(request.getDescription())
                .packType(request.getPackType())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .maxPersons(request.getMaxPersons())
                .imageUrl(request.getImageUrl())
                .image(request.getImage() != null ? request.getImage()
                        : (request.getImages() != null && !request.getImages().isEmpty() ? request.getImages().get(0)
                                : null))
                .images(request.getImages() != null ? request.getImages() : new ArrayList<>())
                .features(request.getFeatures() != null ? request.getFeatures() : new ArrayList<>())
                .inclusions(request.getInclusions() != null ? request.getInclusions() : new ArrayList<>())
                .exclusions(request.getExclusions() != null ? request.getExclusions() : new ArrayList<>())
                .isActive(true)
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .isLimitedOffer(request.getIsLimitedOffer() != null ? request.getIsLimitedOffer() : false)
                .availableQuantity(request.getAvailableQuantity())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .services(new ArrayList<>())
                .build();

        if (request.getSiteId() != null) {
            Site site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Site non trouvé avec l'ID: " + request.getSiteId()));
            pack.setSite(site);
        }

        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            List<CampingService> services = campingServiceRepository.findAllById(request.getServiceIds());
            pack.setServices(services);
        }

        calculateAndValidatePricing(pack);

        pack = packRepository.save(pack);
        return toResponse(pack);
    }

    @Transactional(readOnly = true)
    public PackDTO.Response getById(Long id) {
        tn.esprit.projetintegre.repositories.projections.PackProjection projection = packRepository.findByIdProjected(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pack non trouvé avec l'ID: " + id));
        return toResponse(projection);
    }

    @Transactional(readOnly = true)
    public Page<PackDTO.Response> getAllActive(Pageable pageable) {
        return packRepository.findByIsActiveTrueProjected(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PackDTO.Response> getAllAdmin(Pageable pageable) {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can view all packs");
        }
        return packRepository.findAllProjected(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PackDTO.Response> filterPacks(String siteName, ServiceType serviceType) {
        return packRepository.findByIsActiveTrueAndSite_NameContainingIgnoreCaseAndServices_Type(siteName, serviceType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PackDTO.Response> getFeaturedPacks() {
        return packRepository.findFeaturedPacks().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PackServiceStatsDTO> getActivePacksWithServiceStats() {
        return packRepository.findActivePacksWithServiceStats();
    }

    @Transactional(readOnly = true)
    public List<PackValueRankDTO> getPackValueRanking() {
        List<PackServiceStatsDTO> stats = packRepository.findActivePacksWithServiceStats();
        AtomicInteger rank = new AtomicInteger(1);

        return stats.stream()
                .filter(s -> s.getTotalServicesValue() != null && s.getPackPrice() != null
                          && s.getPackPrice().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparingDouble(s -> {
                    double score = s.getTotalServicesValue()
                            .subtract(s.getPackPrice())
                            .divide(s.getPackPrice(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    return -score;
                }))
                .map(s -> {
                    BigDecimal svcValue = s.getTotalServicesValue() != null ? s.getTotalServicesValue() : BigDecimal.ZERO;
                    BigDecimal savings = svcValue.subtract(s.getPackPrice());
                    double valueScore = BigDecimal.valueOf(savings.doubleValue())
                            .divide(s.getPackPrice(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    double rounded = Math.round(valueScore * 10.0) / 10.0;

                    return new PackValueRankDTO(
                            rank.getAndIncrement(),
                            s.getPackId(),
                            s.getPackName(),
                            s.getPackType(),
                            s.getSiteName(),
                            s.getPackPrice(),
                            svcValue,
                            savings.setScale(2, RoundingMode.HALF_UP),
                            rounded,
                            s.getServiceCount()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public BundleOptimizerResultDTO optimizeBundle(BigDecimal budget, Integer persons) {
        List<PackOptimizerDTO> candidates = packRepository.findPacksForOptimizer()
                .stream()
                .filter(p -> p.getPackPrice() != null && p.getPackPrice().compareTo(budget) <= 0)
                .filter(p -> persons == null || p.getMaxPersons() == null || p.getMaxPersons() >= persons)
                .sorted(Comparator.comparingDouble(p -> -p.getValueScore()))
                .toList();

        if (candidates.isEmpty()) {
            BundleOptimizerResultDTO empty = new BundleOptimizerResultDTO();
            empty.setSelectedPacks(List.of());
            empty.setBudget(budget);
            empty.setPersons(persons);
            empty.setTotalPrice(BigDecimal.ZERO);
            empty.setTotalServicesValue(BigDecimal.ZERO);
            empty.setTotalSavings(BigDecimal.ZERO);
            empty.setRemainingBudget(budget);
            empty.setPackCount(0);
            empty.setMessage("Aucun pack disponible pour ce budget.");
            return empty;
        }

        List<PackOptimizerDTO> selected = new ArrayList<>();
        BigDecimal remaining = budget;

        for (PackOptimizerDTO pack : candidates) {
            if (pack.getPackPrice().compareTo(remaining) <= 0) {
                selected.add(pack);
                remaining = remaining.subtract(pack.getPackPrice());
            }
        }

        BigDecimal totalPrice = budget.subtract(remaining);
        BigDecimal totalServicesValue = selected.stream()
                .map(PackOptimizerDTO::getTotalServicesValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BundleOptimizerResultDTO result = new BundleOptimizerResultDTO();
        result.setSelectedPacks(selected);
        result.setTotalPrice(totalPrice.setScale(2, RoundingMode.HALF_UP));
        result.setTotalServicesValue(totalServicesValue.setScale(2, RoundingMode.HALF_UP));
        result.setTotalSavings(totalServicesValue.subtract(totalPrice).setScale(2, RoundingMode.HALF_UP));
        result.setBudget(budget);
        result.setRemainingBudget(remaining.setScale(2, RoundingMode.HALF_UP));
        result.setPersons(persons);
        result.setPackCount(selected.size());
        result.setMessage(selected.size() + " pack(s) sélectionné(s)");
        return result;
    }

    @Transactional(readOnly = true)
    public List<PackQualityDTO> getPackQualityMetrics() {
        List<PackQualityDTO> metrics = packRepository.findPackQualityMetrics();

        for (PackQualityDTO pack : metrics) {
            try {
                Pack packEntity = packRepository.findById(pack.getPackId()).orElse(null);
                if (packEntity != null && packEntity.getServices() != null) {
                    List<String> allPros = new ArrayList<>();
                    List<String> allCons = new ArrayList<>();

                    for (CampingService service : packEntity.getServices()) {
                        Page<ServiceReview> topReviews = serviceReviewRepository
                                .findMostHelpfulByServiceId(service.getId(), PageRequest.of(0, 2));
                        
                        if (topReviews != null) {
                            for (ServiceReview review : topReviews) {
                                if (review.getPros() != null) allPros.addAll(review.getPros());
                                if (review.getCons() != null) allCons.addAll(review.getCons());
                            }
                        }
                    }

                    pack.setTopPros(allPros.stream().filter(Objects::nonNull).distinct().limit(5).toList());
                    pack.setTopCons(allCons.stream().filter(Objects::nonNull).distinct().limit(5).toList());
                }
            } catch (Exception e) {
                log.error("Error enriching quality metrics for pack {}: {}", pack.getPackId(), e.getMessage());
            }
        }
        return metrics;
    }

    public PackDTO.Response updatePack(Long id, PackDTO.UpdateRequest request) {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can update packs");
        }
        Pack pack = packRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pack non trouvé avec l'ID: " + id));

        if (request.getName() != null) pack.setName(request.getName());
        if (request.getDescription() != null) pack.setDescription(request.getDescription());
        if (request.getPackType() != null) pack.setPackType(request.getPackType());
        if (request.getPrice() != null) pack.setPrice(request.getPrice());
        if (request.getOriginalPrice() != null) pack.setOriginalPrice(request.getOriginalPrice());
        if (request.getDurationDays() != null) pack.setDurationDays(request.getDurationDays());
        if (request.getMaxPersons() != null) pack.setMaxPersons(request.getMaxPersons());
        if (request.getImageUrl() != null) pack.setImageUrl(request.getImageUrl());
        if (request.getServiceIds() != null) {
            List<CampingService> services = campingServiceRepository.findAllById(request.getServiceIds());
            pack.setServices(services);
        }

        calculateAndValidatePricing(pack);
        pack = packRepository.save(pack);
        return toResponse(pack);
    }

    @Transactional
    public void deletePack(Long id) {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can delete packs");
        }
        if (!packRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pack non trouvé avec l'ID: " + id);
        }
        packRepository.deletePackServices(id);
        packRepository.deletePackImages(id);
        packRepository.deletePackFeatures(id);
        packRepository.deletePackInclusions(id);
        packRepository.deletePackExclusions(id);
        packRepository.deletePackById(id);
    }

    private PackDTO.Response toResponse(tn.esprit.projetintegre.repositories.projections.PackProjection projection) {
        return PackDTO.Response.builder()
                .id(projection.getId())
                .name(projection.getName())
                .description(projection.getDescription())
                .packType(projection.getPackType())
                .price(projection.getPrice())
                .originalPrice(projection.getOriginalPrice())
                .discountPercentage(calculateDiscount(projection.getPrice(), projection.getOriginalPrice()))
                .durationDays(projection.getDurationDays())
                .maxPersons(projection.getMaxPersons())
                .isActive(projection.getIsActive())
                .isFeatured(projection.getIsFeatured())
                .isLimitedOffer(projection.getIsLimitedOffer())
                .availableQuantity(projection.getAvailableQuantity())
                .soldCount(projection.getSoldCount())
                .rating(projection.getRating())
                .reviewCount(projection.getReviewCount())
                .validFrom(projection.getValidFrom())
                .validUntil(projection.getValidUntil())
                .siteId(projection.getSiteId())
                .siteName(projection.getSiteName())
                .imageUrl(projection.getImageUrl())
                .serviceCount(projection.getServiceCount())
                .createdAt(projection.getCreatedAt())
                .serviceIds(new ArrayList<>())
                .serviceNames(new ArrayList<>())
                .build();
    }

    private PackDTO.Response toResponse(Pack pack) {
        return PackDTO.Response.builder()
                .id(pack.getId())
                .name(pack.getName())
                .description(pack.getDescription())
                .packType(pack.getPackType())
                .price(pack.getPrice())
                .originalPrice(pack.getOriginalPrice())
                .discountPercentage(pack.getDiscountPercentage())
                .serviceCount(pack.getServices() != null ? pack.getServices().size() : 0)
                .durationDays(pack.getDurationDays())
                .maxPersons(pack.getMaxPersons())
                .isActive(pack.getIsActive())
                .isFeatured(pack.getIsFeatured())
                .isLimitedOffer(pack.getIsLimitedOffer())
                .availableQuantity(pack.getAvailableQuantity())
                .soldCount(pack.getSoldCount())
                .rating(pack.getRating())
                .reviewCount(pack.getReviewCount())
                .validFrom(pack.getValidFrom())
                .validUntil(pack.getValidUntil())
                .siteId(pack.getSite() != null ? pack.getSite().getId() : null)
                .siteName(pack.getSite() != null ? pack.getSite().getName() : null)
                .imageUrl(pack.getImageUrl())
                .createdAt(pack.getCreatedAt())
                .serviceIds(pack.getServices() != null
                        ? pack.getServices().stream().map(CampingService::getId).toList()
                        : new ArrayList<>())
                .serviceNames(pack.getServices() != null
                        ? pack.getServices().stream().map(CampingService::getName).toList()
                        : new ArrayList<>())
                .build();
    }

    public void updateActiveStatus(Long id, boolean active) {
        if (!SecurityUtil.hasRole(Role.ADMIN)) {
            throw new AccessDeniedException("Only ADMIN can update pack status");
        }
        packRepository.updateActiveStatus(id, active);
    }

    private Double calculateDiscount(BigDecimal price, BigDecimal originalPrice) {
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0 && price != null) {
            return originalPrice.subtract(price).divide(originalPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        return 0.0;
    }
}
