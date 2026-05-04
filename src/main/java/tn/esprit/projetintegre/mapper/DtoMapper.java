package tn.esprit.projetintegre.mapper;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import tn.esprit.projetintegre.dto.response.*;
import tn.esprit.projetintegre.entities.*;
import tn.esprit.projetintegre.enums.SponsorTier;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    // Category Mapping
    public CategoryResponse toCategoryResponse(Category entity) {
        if (entity == null)
            return null;
        return CategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .slug(entity.getSlug())
                .icon(null)
                .image(entity.getImage())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parentName(entity.getParent() != null ? entity.getParent().getName() : null)
                .displayOrder(entity.getDisplayOrder())
                .productCount(entity.getProducts() != null ? entity.getProducts().size() : 0)
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<CategoryResponse> toCategoryResponseList(List<Category> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toCategoryResponse).collect(Collectors.toList());
    }

    // Product Mapping
    public ProductResponse toProductResponse(Product entity) {
        if (entity == null)
            return null;
        return ProductResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .originalPrice(entity.getOriginalPrice())
                .discountPercentage(entity.getDiscountPercentage())
                .sku(entity.getSku())
                .barcode(entity.getBarcode())
                .brand(entity.getBrand())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .sellerId(entity.getSeller() != null ? entity.getSeller().getId() : null)
                .sellerName(entity.getSeller() != null ? entity.getSeller().getName() : null)
                .stockQuantity(entity.getStockQuantity())
                .minStockLevel(entity.getMinStockLevel())
                .images(entity.getImages())
                .thumbnail(entity.getThumbnail())
                .rating(entity.getRating())
                .reviewCount(entity.getReviewCount())
                .salesCount(entity.getSalesCount())
                .viewCount(entity.getViewCount())
                .isActive(entity.getIsActive())
                .isFeatured(entity.getIsFeatured())
                .isOnSale(entity.getIsOnSale())
                .isRentable(entity.getIsRentable())
                .rentalPricePerDay(entity.getRentalPricePerDay())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<ProductResponse> toProductResponseList(List<Product> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toProductResponse).collect(Collectors.toList());
    }

    // Mission Mapping
    public MissionResponse toMissionResponse(Mission entity) {
        if (entity == null)
            return null;
        return MissionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .targetValue(entity.getTargetValue())
                .rewardPoints(entity.getRewardPoints())
                .rewardBadge(entity.getRewardBadge())
                .rewardDescription(entity.getRewardDescription())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isActive(entity.getIsActive())
                .isRepeatable(entity.getIsRepeatable())
                .category(entity.getCategory())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<MissionResponse> toMissionResponseList(List<Mission> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toMissionResponse).collect(Collectors.toList());
    }

    // --- UserMission Mapping (complétée) ---
    public UserMissionResponse toUserMissionResponse(UserMission entity) {
        if (entity == null)
            return null;
        return UserMissionResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .missionId(entity.getMission() != null ? entity.getMission().getId() : null)
                .missionName(entity.getMission() != null ? entity.getMission().getName() : null)
                .currentProgress(entity.getCurrentProgress())
                .targetValue(entity.getMission() != null ? entity.getMission().getTargetValue() : 0)
                .isCompleted(entity.getIsCompleted())
                .rewardClaimed(entity.getRewardClaimed())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .rewardClaimedAt(entity.getRewardClaimedAt())
                .build();
    }

    // Notification Mapping
    public NotificationResponse toNotificationResponse(Notification entity) {
        if (entity == null)
            return null;
        return NotificationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .title(entity.getTitle())
                .message(entity.getMessage())
                .type(entity.getType())
                .isRead(entity.getIsRead())
                .actionUrl(entity.getActionUrl())
                .createdAt(entity.getCreatedAt())

                .build();
    }

    // Sponsorship Mapping
    public SponsorshipResponse toSponsorshipResponse(Sponsorship entity) {
        if (entity == null)
            return null;
        return SponsorshipResponse.builder()
                .id(entity.getId())
                .sponsorId(entity.getSponsor() != null ? entity.getSponsor().getId() : null)
                .sponsorName(entity.getSponsor() != null ? entity.getSponsor().getName() : null)
                .eventId(entity.getEvent() != null ? entity.getEvent().getId() : null)
                .eventTitle(entity.getEvent() != null ? entity.getEvent().getTitle() : null)
                .sponsorshipType(entity.getSponsorshipType())
                .sponsorshipLevel(entity.getSponsorshipLevel())
                .description(entity.getDescription())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isPaid(entity.getIsPaid())
                .paidAt(entity.getPaidAt())
                .benefits(entity.getBenefits())
                .deliverables(entity.getDeliverables())
                .isActive(entity.getIsActive())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<SponsorshipResponse> toSponsorshipResponseList(List<Sponsorship> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toSponsorshipResponse).collect(Collectors.toList());
    }

    // Subscription Mapping
    public SubscriptionResponse toSubscriptionResponse(Subscription entity) {
        if (entity == null)
            return null;
        return SubscriptionResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .planName(entity.getPlanName())
                .planDescription(entity.getPlanType()) // planType de l'entité -> planDescription du DTO
                .price(entity.getPrice())
                .billingCycle(entity.getPlanType()) // planType de l'entité -> billingCycle du DTO
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .nextBillingDate(entity.getRenewalDate()) // renewalDate de l'entité -> nextBillingDate du DTO
                .autoRenew(entity.getAutoRenew())
                .isTrial(false) // Champ non présent dans l'entité
                .trialEndDate(null) // Champ non présent dans l'entité
                .cancelledAt(null) // Champ non présent dans l'entité
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<SubscriptionResponse> toSubscriptionResponseList(List<Subscription> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toSubscriptionResponse).collect(Collectors.toList());
    }

    // Transaction Mapping
    // Transaction Mapping (corrigée)
    public TransactionResponse toTransactionResponse(Transaction entity) {
        if (entity == null)
            return null;
        return TransactionResponse.builder()
                .id(entity.getId())
                .transactionNumber(entity.getTransactionNumber())
                .amount(entity.getAmount())
                .type(entity.getType())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .referenceType(entity.getReferenceType())
                .referenceId(entity.getReferenceId())
                .walletId(entity.getWallet() != null ? entity.getWallet().getId() : null)
                .userId(entity.getWallet() != null && entity.getWallet().getUser() != null
                        ? entity.getWallet().getUser().getId()
                        : null)
                .createdAt(entity.getCreatedAt())
                // Supprimé car absent du DTO
                .build();
    }

    public List<TransactionResponse> toTransactionResponseList(List<Transaction> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toTransactionResponse).collect(Collectors.toList());
    }

    // Wallet Mapping (complétée)
    public List<WalletResponse> toWalletResponseList(List<Wallet> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toWalletResponse).collect(Collectors.toList());
    }

    // Wishlist Mapping
    public WishlistResponse toWishlistResponse(Wishlist entity) {
        if (entity == null)
            return null;
        return WishlistResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .name(entity.getName())
                .isPublic(entity.getIsPublic())
                .productCount(entity.getProducts() != null ? entity.getProducts().size() : 0)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<WishlistResponse> toWishlistResponseList(List<Wishlist> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toWishlistResponse).collect(Collectors.toList());
    }

    // Ticket Mapping (complété)
    public List<TicketResponse> toTicketResponseList(List<Ticket> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toTicketResponse).collect(Collectors.toList());
    }

    // Ticket Mapping
    public TicketResponse toTicketResponse(Ticket entity) {
        if (entity == null)
            return null;
        return TicketResponse.builder()
                .id(entity.getId())
                .ticketNumber(entity.getTicketNumber())
                .ticketType(entity.getTicketType())
                .price(entity.getPrice())
                .status(entity.getStatus()) // Utilise l'enum TicketStatus
                .eventId(entity.getEvent() != null ? entity.getEvent().getId() : null)
                .eventTitle(entity.getEvent() != null ? entity.getEvent().getTitle() : null)
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .usedAt(entity.getUsedAt())
                .createdAt(entity.getCreatedAt())
                .purchasedAt(entity.getCreatedAt()) // Souvent identique à createdAt si pas de champ spécifique
                .build();
    }

    public List<NotificationResponse> toNotificationResponseList(List<Notification> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toNotificationResponse).collect(Collectors.toList());
    }

    public List<UserMissionResponse> toUserMissionResponseList(List<UserMission> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toUserMissionResponse).collect(Collectors.toList());
    }

    // Coupon Mapping
    public CouponResponse toCouponResponse(Coupon entity) {
        if (entity == null)
            return null;
        return CouponResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .discountType(entity.getType() != null ? entity.getType().name() : null)
                .discountValue(entity.getDiscountValue())
                .minOrderAmount(entity.getMinOrderAmount()) // Corrigé
                .maxDiscountAmount(entity.getMaxDiscountAmount())
                .usageLimit(entity.getUsageLimit()) // Corrigé
                .currentUsage(entity.getUsageCount()) // Corrigé
                .usageLimitPerUser(entity.getUsageLimitPerUser())
                .validFrom(entity.getValidFrom()) // Corrigé
                .validUntil(entity.getValidUntil()) // Corrigé
                .isActive(entity.getIsActive())
                .isValid(entity.isValid())
                .applicableCategoryId(
                        entity.getApplicableCategory() != null ? entity.getApplicableCategory().getId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<CouponResponse> toCouponResponseList(List<Coupon> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toCouponResponse).collect(Collectors.toList());
    }

    // Event Mapping
    public EventResponse toEventResponse(Event entity) {
        if (entity == null)
            return null;
        return EventResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .eventType(entity.getEventType())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .location(entity.getLocation())
                .maxParticipants(entity.getMaxParticipants())
                .currentParticipants(entity.getCurrentParticipants())
                .price(entity.getPrice())
                .status(entity.getStatus())
                .images(entity.getImages())
                .thumbnail(entity.getThumbnail())
                .siteId(entity.getSite() != null ? entity.getSite().getId() : null)
                .siteName(entity.getSite() != null ? entity.getSite().getName() : null)
                .organizerId(entity.getOrganizer() != null ? entity.getOrganizer().getId() : null)
                .organizerName(entity.getOrganizer() != null ? entity.getOrganizer().getCompanyName() : null)
                .isFree(entity.getIsFree())
                .viewCount(entity.getViewCount())
                .gamifications(toGamificationResponseList(entity.getBadges()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<EventResponse> toEventResponseList(List<Event> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toEventResponse).collect(Collectors.toList());
    }

    public EventCommentResponse toEventCommentResponse(EventComment entity) {
        if (entity == null)
            return null;
        String userName = null;
        if (entity.getUser() != null) {
            userName = entity.getUser().getName() != null && !entity.getUser().getName().isBlank()
                    ? entity.getUser().getName()
                    : entity.getUser().getUsername();
        }

        return EventCommentResponse.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .eventId(entity.getEvent() != null ? entity.getEvent().getId() : null)
                .eventTitle(entity.getEvent() != null ? entity.getEvent().getTitle() : null)
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(userName)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<EventCommentResponse> toEventCommentResponseList(List<EventComment> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toEventCommentResponse).collect(Collectors.toList());
    }

    public InventoryResponse toInventoryResponse(Inventory entity) {
        if (entity == null)
            return null;

        Integer quantity = entity.getQuantity() != null ? entity.getQuantity() : 0;
        Integer reservedQuantity = entity.getReservedQuantity() != null ? entity.getReservedQuantity() : 0;
        Integer availableQuantity = entity.getAvailableQuantity() != null
                ? entity.getAvailableQuantity()
                : quantity - reservedQuantity;
        boolean isLowStock = entity.getLowStockThreshold() != null
                && availableQuantity <= entity.getLowStockThreshold();

        return InventoryResponse.builder()
                .id(entity.getId())
                .sku(entity.getSku())
                .quantity(quantity)
                .reservedQuantity(reservedQuantity)
                .availableQuantity(availableQuantity)
                .lowStockThreshold(entity.getLowStockThreshold())
                .safetyStock(entity.getSafetyStock())
                .reorderQuantity(entity.getReorderQuantity())
                .location(entity.getLocation())
                .aisle(entity.getAisle())
                .shelf(entity.getShelf())
                .bin(entity.getBin())
                .lastStockCheck(entity.getLastStockCheck())
                .lastRestocked(entity.getLastRestocked())
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .variantId(entity.getVariant() != null ? entity.getVariant().getId() : null)
                .variantName(entity.getVariant() != null ? entity.getVariant().getName() : null)
                .warehouseId(entity.getWarehouse() != null ? entity.getWarehouse().getId() : null)
                .warehouseName(entity.getWarehouse() != null ? entity.getWarehouse().getName() : null)
                .isLowStock(isLowStock)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<InventoryResponse> toInventoryResponseList(List<Inventory> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toInventoryResponse).collect(Collectors.toList());
    }

    // Site Mapping
    public SiteResponse toSiteResponse(Site entity) {
        if (entity == null)
            return null;
        return SiteResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .address(entity.getAddress())
                .city(entity.getCity())
                .country(entity.getCountry())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .capacity(entity.getCapacity())
                .pricePerNight(entity.getPricePerNight())
                .images(entity.getImages())
                .amenities(entity.getAmenities())
                .contactPhone(entity.getContactPhone())
                .contactEmail(entity.getContactEmail())
                .isActive(entity.getIsActive())
                .checkInTime(entity.getCheckInTime())
                .checkOutTime(entity.getCheckOutTime())
                .houseRules(entity.getHouseRules())
                .rating(entity.getRating())
                .reviewCount(entity.getReviewCount())
                .ownerId(entity.getOwner() != null ? entity.getOwner().getId() : null)
                .ownerName(entity.getOwner() != null ? entity.getOwner().getName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    // Gamification Mapping (mapping Badge to GamificationResponse for frontend
    // compatibility)
    public GamificationResponse toGamificationResponse(Badge entity) {
        if (entity == null)
            return null;
        return GamificationResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .build();
    }

    public List<GamificationResponse> toGamificationResponseList(java.util.Collection<Badge> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toGamificationResponse).collect(Collectors.toList());
    }

    // Achievement Mapping
    public AchievementResponse toAchievementResponse(Achievement entity) {
        if (entity == null)
            return null;
        return AchievementResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .badge(entity.getBadge())
                .icon(entity.getIcon())
                .requiredPoints(entity.getRequiredPoints())
                .rewardPoints(entity.getRewardPoints())
                .category(entity.getCategory())
                .level(entity.getLevel())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    // Liste de Achievement
    public List<AchievementResponse> toAchievementResponseList(List<Achievement> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toAchievementResponse).collect(Collectors.toList());
    }

    // Order Mapping
    public OrderResponse toOrderResponse(Order entity) {
        if (entity == null)
            return null;
        return OrderResponse.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .totalPrice(entity.getTotalAmount()) // Utilise totalAmount de l'entité pour totalPrice du DTO
                .subtotal(entity.getSubtotal())
                .discountAmount(entity.getDiscountAmount())
                .taxAmount(entity.getTaxAmount())
                .shippingCost(entity.getShippingCost())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .paymentStatus(entity.getPaymentStatus())
                .paymentMethod(entity.getPaymentMethod())
                .shippingAddress(entity.getShippingAddress())
                .shippingCity(entity.getShippingCity())
                .shippingCountry(entity.getShippingCountry())
                .shippingPhone(entity.getShippingPhone())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deliveredAt(entity.getDeliveredAt())
                .build();
    }

    // Promotion Mapping
    public PromotionResponse toPromotionResponse(Promotion entity) {
        if (entity == null)
            return null;
        return PromotionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .discountValue(entity.getDiscountValue())
                .minPurchaseAmount(entity.getMinPurchaseAmount())
                .maxDiscountAmount(entity.getMaxDiscountAmount())
                .maxUsage(entity.getMaxUsage())
                .currentUsage(entity.getCurrentUsage())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isActive(entity.getIsActive())
                .applicableProductIds(entity.getApplicableProductIds())
                .applicableCategoryIds(entity.getApplicableCategoryIds())
                .targetAudience(entity.getTargetAudience())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<PromotionResponse> toPromotionResponseList(List<Promotion> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toPromotionResponse).collect(Collectors.toList());
    }

    // Reservation Mapping

    // Reservation Mapping
    public ReservationResponse toReservationResponse(Reservation entity) {
        if (entity == null)
            return null;
        return ReservationResponse.builder()
                .id(entity.getId())
                .reservationNumber(entity.getReservationNumber())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .siteId(entity.getSite() != null ? entity.getSite().getId() : null)
                .siteName(entity.getSite() != null ? entity.getSite().getName() : null)
                // Conversion LocalDateTime vers LocalDate pour le DTO
                .checkInDate(entity.getCheckInDate() != null ? entity.getCheckInDate().toLocalDate() : null)
                .checkOutDate(entity.getCheckOutDate() != null ? entity.getCheckOutDate().toLocalDate() : null)
                .numberOfNights(entity.getNumberOfNights())
                .numberOfGuests(entity.getNumberOfGuests())
                .pricePerNight(entity.getPricePerNight())
                .totalPrice(entity.getTotalPrice())
                .status(entity.getStatus())
                .paymentStatus(entity.getPaymentStatus())
                .specialRequests(entity.getSpecialRequests())
                .contactPhone(entity.getGuestPhone()) // Mappé guestPhone vers contactPhone
                .contactEmail(entity.getGuestEmail()) // Mappé guestEmail vers contactEmail
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<ReservationResponse> toReservationResponseList(List<Reservation> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toReservationResponse).collect(Collectors.toList());
    }

    // Site Mapping
    public List<SiteResponse> toSiteResponseList(List<Site> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toSiteResponse).collect(Collectors.toList());
    }

    // --- Sponsor Mapping ---
    public SponsorResponse toSponsorResponse(Sponsor entity) {
        if (entity == null)
            return null;
        return SponsorResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .logo(entity.getLogo())
                .website(entity.getWebsite())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .city(entity.getCity())
                .country(entity.getCountry())
                .contactPerson(entity.getContactPerson())
                .contactPosition(entity.getContactPosition())
                .notes(entity.getNotes())
                .tier(entity.getTier() != null ? entity.getTier() : SponsorTier.BRONZE)
                .isActive(entity.getIsActive())
                // Note: address, city, country, contactPosition, notes ne sont pas dans
                // l'entité Sponsor fournie
                // sponsorshipCount peut être calculé si la relation existe :
                .sponsorshipCount(entity.getSponsorships() != null && Hibernate.isInitialized(entity.getSponsorships())
                        ? entity.getSponsorships().size()
                        : 0)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    // Sponsor Mapping
    public List<SponsorResponse> toSponsorResponseList(List<Sponsor> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toSponsorResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> toOrderResponseList(List<Order> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toOrderResponse).collect(Collectors.toList());
    }

    // Cart Mapping
    public CartResponse toCartResponse(Cart entity) {
        if (entity == null)
            return null;
        return CartResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .items(toCartItemResponseList(entity.getItems()))
                .totalAmount(entity.getTotalAmount())
                .discountAmount(entity.getDiscountAmount())
                .finalAmount(entity.getTotalAmount() != null && entity.getDiscountAmount() != null
                        ? entity.getTotalAmount().subtract(entity.getDiscountAmount())
                        : entity.getTotalAmount())
                .totalItems(entity.getItems() != null ? entity.getItems().size() : 0)
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CartItemResponse toCartItemResponse(CartItem entity) {
        if (entity == null)
            return null;
        
        String name = "";
        String thumbnail = null;
        Long productId = null;
        Integer stock = 0;

        if (entity.getProduct() != null) {
            name = entity.getProduct().getName();
            thumbnail = entity.getProduct().getThumbnail();
            productId = entity.getProduct().getId();
            stock = entity.getProduct().getStockQuantity();
        } else if (entity.getPack() != null) {
            name = entity.getPack().getName();
            thumbnail = entity.getPack().getImage(); // or getImage() depending on which one is used
            productId = entity.getPack().getId();
            stock = entity.getPack().getAvailableQuantity();
        }

        return CartItemResponse.builder()
                .id(entity.getId())
                .productId(productId)
                .productName(name)
                .productThumbnail(thumbnail)
                .quantity(entity.getQuantity())
                .unitPrice(entity.getPrice())
                .subtotal(entity.getPrice() != null && entity.getQuantity() != null
                        ? entity.getPrice().multiply(java.math.BigDecimal.valueOf(entity.getQuantity()))
                        : java.math.BigDecimal.ZERO)
                .stockAvailable(stock)
                .build();
    }

    // UserAchievement Mapping
    public UserAchievementResponse toUserAchievementResponse(UserAchievement entity) {
        if (entity == null)
            return null;
        return UserAchievementResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .achievementId(entity.getAchievement() != null ? entity.getAchievement().getId() : null)
                .achievementName(entity.getAchievement() != null ? entity.getAchievement().getName() : null)
                .achievementBadge(entity.getAchievement() != null ? entity.getAchievement().getBadge() : null)
                .achievementIcon(entity.getAchievement() != null ? entity.getAchievement().getIcon() : null)
                .achievementDescription(
                        entity.getAchievement() != null ? entity.getAchievement().getDescription() : null)
                .rewardPoints(entity.getAchievement() != null ? entity.getAchievement().getRewardPoints() : null)
                .isDisplayed(entity.getIsDisplayed())
                .unlockedAt(entity.getUnlockedAt())
                .build();
    }

    public List<UserAchievementResponse> toUserAchievementResponseList(List<UserAchievement> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toUserAchievementResponse).collect(Collectors.toList());
    }

    public List<CartItemResponse> toCartItemResponseList(List<CartItem> entities) {
        if (entities == null)
            return java.util.Collections.emptyList();
        return entities.stream().map(this::toCartItemResponse).collect(java.util.stream.Collectors.toList());
    }

    // CampingService Mapping
    public CampingServiceResponse toCampingServiceResponse(CampingService entity) {
        if (entity == null)
            return null;
        return CampingServiceResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .price(entity.getPrice())
                .pricingUnit(entity.getPricingUnit())
                .images(entity.getImages())
                .isActive(entity.getIsActive())
                .isAvailable(entity.getIsAvailable())
                .isOrganizerService(entity.getIsOrganizerService())
                .isCamperOnly(entity.getIsCamperOnly())
                .maxCapacity(entity.getMaxCapacity())
                .duration(entity.getDuration())
                .rating(entity.getRating())
                .reviewCount(entity.getReviewCount())
                .providerId(entity.getProvider() != null ? entity.getProvider().getId() : null)
                .providerName(entity.getProvider() != null ? entity.getProvider().getName() : null)
                .siteId(entity.getSite() != null ? entity.getSite().getId() : null)
                .siteName(entity.getSite() != null ? entity.getSite().getName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<CampingServiceResponse> toCampingServiceResponseList(List<CampingService> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toCampingServiceResponse).collect(Collectors.toList());
    }

    // Alert Mapping
    public AlertResponse toAlertResponse(Alert entity) {
        if (entity == null)
            return null;
        return AlertResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .alertType(entity.getAlertType())
                .severity(entity.getSeverity())
                .status(entity.getStatus())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .location(entity.getLocation())
                .reportedById(entity.getReportedBy() != null ? entity.getReportedBy().getId() : null)
                .reportedByName(entity.getReportedBy() != null ? entity.getReportedBy().getName() : null)
                .siteId(entity.getSite() != null ? entity.getSite().getId() : null)
                .siteName(entity.getSite() != null ? entity.getSite().getName() : null)
                .resolvedById(entity.getResolvedBy() != null ? entity.getResolvedBy().getId() : null)
                .resolvedByName(entity.getResolvedBy() != null ? entity.getResolvedBy().getName() : null)
                .resolutionNotes(entity.getResolutionNotes())
                .resolvedAt(entity.getResolvedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<AlertResponse> toAlertResponseList(List<Alert> entities) {
        if (entities == null)
            return Collections.emptyList();
        return entities.stream().map(this::toAlertResponse).collect(Collectors.toList());
    }

    // Wallet Mapping
    public WalletResponse toWalletResponse(Wallet entity) {
        if (entity == null)
            return null;
        return WalletResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getName() : null)
                .balance(entity.getBalance())
                .currency(entity.getCurrency())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
