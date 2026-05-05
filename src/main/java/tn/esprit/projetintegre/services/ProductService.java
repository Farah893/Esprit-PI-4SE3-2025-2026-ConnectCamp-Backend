package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.esprit.projetintegre.config.TaxConfiguration;
import tn.esprit.projetintegre.dto.request.PriceCalculationRequest;
import tn.esprit.projetintegre.dto.response.PriceCalculationResponse;
import tn.esprit.projetintegre.dto.response.ProductQualityScoreResponse;
import tn.esprit.projetintegre.dto.response.QualityScoreBreakdown;
import tn.esprit.projetintegre.dto.response.ShippingBreakdown;
import tn.esprit.projetintegre.dto.response.TaxBreakdown;
import tn.esprit.projetintegre.entities.Category;
import tn.esprit.projetintegre.entities.Product;
import tn.esprit.projetintegre.entities.ProductVariant;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.enums.CountryTaxZone;
import tn.esprit.projetintegre.enums.QualityBadge;
import tn.esprit.projetintegre.enums.ShippingZone;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.CategoryRepository;
import tn.esprit.projetintegre.repositories.ProductRepository;
import tn.esprit.projetintegre.repositories.ProductVariantRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final CurrencyService currencyService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final TaxConfiguration taxConfiguration;

    // ============================================================
    // CRUD & standard methods
    // ============================================================

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    public Page<Product> getProductsBySeller(Long sellerId, Pageable pageable) {
        return productRepository.findBySellerId(sellerId, pageable);
    }

    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable);
    }

    public Page<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable);
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrue();
    }

    public List<Product> getTopSellingProducts(int limit) {
        return productRepository.findTopSellingProducts(PageRequest.of(0, limit));
    }

    @Transactional
    public Product createProduct(Product product, Long categoryId, Long sellerId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }
        if (sellerId != null) {
            User seller = userRepository.findById(sellerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
            product.setSeller(seller);
        }
        return productRepository.save(product);
    }

    @CacheEvict(value = "product-quality-score", key = "#id")
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);

        if (productDetails.getName() != null)          product.setName(productDetails.getName());
        if (productDetails.getDescription() != null)   product.setDescription(productDetails.getDescription());
        if (productDetails.getPrice() != null)         product.setPrice(productDetails.getPrice());
        if (productDetails.getOriginalPrice() != null) product.setOriginalPrice(productDetails.getOriginalPrice());
        if (productDetails.getStockQuantity() != null) product.setStockQuantity(productDetails.getStockQuantity());
        if (productDetails.getSku() != null)           product.setSku(productDetails.getSku());
        if (productDetails.getBrand() != null)         product.setBrand(productDetails.getBrand());
        if (productDetails.getImages() != null)        product.setImages(productDetails.getImages());
        if (productDetails.getThumbnail() != null)     product.setThumbnail(productDetails.getThumbnail());
        if (productDetails.getIsActive() != null)      product.setIsActive(productDetails.getIsActive());
        if (productDetails.getIsFeatured() != null)    product.setIsFeatured(productDetails.getIsFeatured());
        if (productDetails.getIsOnSale() != null)      product.setIsOnSale(productDetails.getIsOnSale());

        return productRepository.save(product);
    }

    @Transactional
    public Product updateStock(Long id, int quantity) {
        Product product = getProductById(id);
        product.setStockQuantity(product.getStockQuantity() + quantity);
        return productRepository.save(product);
    }

    @Transactional
    public void incrementViewCount(Long id) {
        Product product = getProductById(id);
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
    }

    @CacheEvict(value = "product-quality-score", key = "#id")
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setIsActive(false);
        productRepository.save(product);
    }

    // ============================================================
    // Quality Score API
    // ============================================================

    @Cacheable(value = "product-quality-score", key = "#productId", unless = "#result == null")
    @Transactional(readOnly = true)
    public ProductQualityScoreResponse calculateQualityScore(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return buildQualityScoreResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductQualityScoreResponse> calculateQualityScoresBatch(List<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        return products.stream()
                .map(this::buildQualityScoreResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductQualityScoreResponse> getTopQualityProducts(Pageable pageable) {
        List<Product> allProducts = productRepository.findAll();

        List<ProductQualityScoreResponse> scored = allProducts.stream()
                .map(this::buildQualityScoreResponse)
                .sorted((a, b) -> Integer.compare(b.getOverallScore(), a.getOverallScore()))
                .collect(Collectors.toList());

        int total = scored.size();
        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), total);

        List<ProductQualityScoreResponse> pageContent = (start >= total)
                ? List.of()
                : scored.subList(start, end);

        return new PageImpl<>(pageContent, pageable, total);
    }

    // ============================================================
    // Price Calculation API (TVA + Shipping)
    // ============================================================
    @Cacheable(
            value = "price-calculation",
            key = "#request.productId + '-' + #request.countryCode + '-' + #request.variantId + '-' + #request.currency",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public PriceCalculationResponse calculatePrice(PriceCalculationRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + request.getVariantId()));
        }

        // 🌍 Pays
        String countryCode = (request.getCountryCode() != null && !request.getCountryCode().isBlank())
                ? request.getCountryCode().toUpperCase()
                : "FR";

        // 💱 Currency AUTO (fix ici 🔥)
        String targetCurrency = (request.getCurrency() != null && !request.getCurrency().isBlank())
                ? request.getCurrency().toUpperCase()
                : resolveCurrency(countryCode);

        // 💰 Calcul en EUR (interne)
        BigDecimal basePrice = resolveBasePrice(product, variant);
        BigDecimal discountAmount = resolveDiscount(product, variant, basePrice);

        BigDecimal discountPct = basePrice.compareTo(BigDecimal.ZERO) > 0
                ? discountAmount.multiply(new BigDecimal("100"))
                .divide(basePrice, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal priceAfterDiscount = basePrice.subtract(discountAmount).max(BigDecimal.ZERO);

        TaxBreakdown tax = buildTaxBreakdown(priceAfterDiscount, countryCode);
        BigDecimal customs = computeCustoms(priceAfterDiscount, "TN", countryCode);

        ShippingBreakdown shipping = buildShippingBreakdown(
                resolveWeight(product, variant),
                countryCode,
                priceAfterDiscount
        );

        BigDecimal totalPriceEUR = priceAfterDiscount
                .add(tax.getAmount())
                .add(customs)
                .add(shipping.getFinalCost())
                .setScale(2, RoundingMode.HALF_UP);

        int availableQty = resolveStock(product, variant);

        // 💱 Conversion
        BigDecimal baseConverted = currencyService.convert(basePrice, "EUR", targetCurrency);
        BigDecimal discountConverted = currencyService.convert(discountAmount, "EUR", targetCurrency);
        BigDecimal afterDiscountConv = currencyService.convert(priceAfterDiscount, "EUR", targetCurrency);
        BigDecimal taxConverted = currencyService.convert(tax.getAmount(), "EUR", targetCurrency);
        BigDecimal shippingConverted = currencyService.convert(shipping.getFinalCost(), "EUR", targetCurrency);
        BigDecimal customsConverted = currencyService.convert(customs, "EUR", targetCurrency);
        BigDecimal totalConverted = currencyService.convert(totalPriceEUR, "EUR", targetCurrency);

        // ❌ SUPPRESSION toBuilder → ✔️ rebuild manuel
        TaxBreakdown newTax = TaxBreakdown.builder()
                .zone(tax.getZone())
                .rate(tax.getRate())
                .amount(taxConverted)
                .build();

        ShippingBreakdown newShipping = ShippingBreakdown.builder()
                .zone(shipping.getZone())
                .multiplier(shipping.getMultiplier())
                .baseCost(shipping.getBaseCost())
                .finalCost(shippingConverted)
                .isFree(shipping.getIsFree())
                .freeShippingReason(shipping.getFreeShippingReason())
                .estimatedDeliveryMin(shipping.getEstimatedDeliveryMin())
                .estimatedDeliveryMax(shipping.getEstimatedDeliveryMax())
                .build();

        return PriceCalculationResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .variantId(variant != null ? variant.getId() : null)

                .requestedCountry(countryCode)
                .requestedPostalCode(request.getPostalCode())

                .basePrice(baseConverted)
                .discountAmount(discountConverted)
                .discountPercentage(discountPct)
                .priceAfterDiscount(afterDiscountConv)

                .tax(newTax)
                .customsFees(customsConverted)
                .shipping(newShipping)

                .subtotalHT(afterDiscountConv)
                .totalTax(taxConverted)
                .totalShipping(shippingConverted)
                .totalCustoms(customsConverted)
                .totalPrice(totalConverted)

                .inStock(availableQty > 0)
                .availableQuantity(availableQty)

                .currency(targetCurrency) // 🔥 dynamique
                .calculatedAt(Instant.now())

                .build();
    }
    @Transactional(readOnly = true)
    public List<PriceCalculationResponse> calculatePricesBatch(List<PriceCalculationRequest> requests) {
        return requests.stream()
                .map(req -> calculatePrice(req)) // ✔️ FIX
                .collect(Collectors.toList());
    }
    private String resolveCurrency(String countryCode) {
        if (countryCode == null) return "EUR";

        return switch (countryCode) {
            case "TN" -> "TND"; // Tunisie
            case "US" -> "USD";
            case "GB" -> "GBP";
            case "DE", "FR", "ES", "IT" -> "EUR";
            case "AE" -> "AED";
            case "CA" -> "CAD";
            case "JP" -> "JPY";
            case "CN" -> "CNY";
            default -> "EUR";
        };
    }

    public List<Map<String, Object>> getAvailableCountries() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (CountryTaxZone zone : CountryTaxZone.values()) {
            if ("OTHER".equals(zone.getCountryCode()) || "EU_OTHER".equals(zone.getCountryCode())) continue;
            ShippingZone shippingZone = ShippingZone.fromCountryCode(zone.getCountryCode());
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("countryCode",   zone.getCountryCode());
            entry.put("label",         zone.getLabel());
            entry.put("vatRate",       zone.getStandardVatRate());
            entry.put("isEU",          zone.isEU());
            entry.put("shippingZone",  shippingZone.name());
            entry.put("shippingLabel", shippingZone.getLabel());
            result.add(entry);
        }
        return result;
    }

    // ============================================================
    // Private helpers — quality score
    // ============================================================

    private ProductQualityScoreResponse buildQualityScoreResponse(Product product) {
        int completeness = calculateCompletenessScore(product);
        int media        = calculateMediaScore(product);
        int review       = calculateReviewScore(product);
        int performance  = calculatePerformanceScore(product);
        int seller       = calculateSellerScore(product);
        int overall      = completeness + media + review + performance + seller;
        QualityBadge badge = QualityBadge.fromScore(overall);

        return ProductQualityScoreResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .overallScore(overall)
                .badge(badge)
                .badgeColor(badge.getColor())
                .breakdown(buildBreakdown(completeness, media, review, performance, seller))
                .isActive(product.getStockQuantity() != null && product.getStockQuantity() > 0)
                .build();
    }

    private int calculateCompletenessScore(Product product) {
        int score = 0;
        if (product.getName() != null && product.getName().length() >= 10) score += 3;
        if (product.getName() != null && product.getName().length() >= 30) score += 2;
        if (product.getDescription() != null && product.getDescription().length() >= 50)  score += 5;
        if (product.getDescription() != null && product.getDescription().length() >= 200) score += 5;
        if (product.getSku() != null && !product.getSku().isBlank())         score += 3;
        if (product.getBarcode() != null && !product.getBarcode().isBlank()) score += 2;
        if (product.getCategory() != null) score += 5;
        return Math.min(score, 25);
    }

    private int calculateMediaScore(Product product) {
        int score = 10;
        if (product.getThumbnail() != null && !product.getThumbnail().isBlank()) score += 5;
        int imageCount = (product.getImages() != null) ? product.getImages().size() : 0;
        if (imageCount >= 1) score += 3;
        if (imageCount >= 3) score += 4;
        if (imageCount >= 5) score += 3;
        return Math.min(score, 15);
    }

    private int calculateReviewScore(Product product) {
        int score = 10;
        if (product.getRating() != null) {
            score += (int) ((product.getRating().doubleValue() / 5.0) * 15);
        }
        int reviewCount = (product.getReviewCount() != null) ? product.getReviewCount() : 0;
        if (reviewCount >= 1)  score += 2;
        if (reviewCount >= 5)  score += 2;
        if (reviewCount >= 20) score += 1;
        if (reviewCount > 0 && product.getReviews() != null) {
            long verified = product.getReviews().stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsVerifiedPurchase()))
                    .count();
            score += (int) Math.round((double) verified / reviewCount * 5);
        }
        return Math.min(score, 25);
    }

    private int calculatePerformanceScore(Product product) {
        int score = 10;
        int viewCount  = (product.getViewCount()  != null) ? product.getViewCount()  : 0;
        int salesCount = (product.getSalesCount() != null) ? product.getSalesCount() : 0;
        if (viewCount > 0) {
            double cr = (double) salesCount / viewCount * 100;
            if (cr >= 1.0) score += 4;
            if (cr >= 3.0) score += 3;
            if (cr >= 5.0) score += 3;
        }
        long days = (product.getCreatedAt() != null)
                ? ChronoUnit.DAYS.between(product.getCreatedAt(), LocalDateTime.now()) : 1L;
        double spd = (double) salesCount / Math.max(days, 1);
        if (spd >= 0.5) score += 3;
        if (spd >= 2.0) score += 2;
        if (product.getStockQuantity() != null && product.getStockQuantity() > 0) score += 5;
        return Math.min(score, 20);
    }

    private int calculateSellerScore(Product product) {
        int score = 10;
        User seller = product.getSeller();
        if (seller != null) {
            if (seller.getSellerRating() != null)
                score += (int) Math.round((seller.getSellerRating().doubleValue() / 5.0) * 8);
            if (Boolean.TRUE.equals(seller.getSellerVerified())) score += 4;
            if (seller.getEmail()     != null && !seller.getEmail().isBlank())     score += 1;
            if (seller.getPhone()     != null && !seller.getPhone().isBlank())     score += 1;
            if (seller.getStoreName() != null && !seller.getStoreName().isBlank()) score += 1;
        }
        return Math.min(score, 15);
    }

    private QualityScoreBreakdown buildBreakdown(int c, int m, int r, int p, int s) {
        return QualityScoreBreakdown.builder()
                .completenessScore(c).mediaScore(m).reviewScore(r)
                .performanceScore(p).sellerScore(s).build();
    }

    // ============================================================
    // Private helpers — price calculation
    // ============================================================

    private BigDecimal resolveBasePrice(Product product, ProductVariant variant) {
        if (variant != null && variant.getPrice() != null) return variant.getPrice();
        return product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
    }

    private BigDecimal resolveDiscount(Product product, ProductVariant variant, BigDecimal basePrice) {
        BigDecimal discount = BigDecimal.ZERO;
        if (product.getDiscountPercentage() != null
                && product.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            discount = basePrice.multiply(product.getDiscountPercentage())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        if (variant != null && variant.getCompareAtPrice() != null && variant.getPrice() != null
                && variant.getCompareAtPrice().compareTo(variant.getPrice()) > 0) {
            discount = discount.max(variant.getCompareAtPrice().subtract(variant.getPrice()));
        }
        return discount;
    }

    private TaxBreakdown buildTaxBreakdown(BigDecimal priceAfterDiscount, String countryCode) {
        CountryTaxZone taxZone = CountryTaxZone.fromCountryCode(countryCode);
        BigDecimal taxRate = new BigDecimal(String.valueOf(taxZone.getStandardVatRate()));
        BigDecimal taxAmount = priceAfterDiscount
                .multiply(taxRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
        return TaxBreakdown.builder().zone(taxZone).rate(taxRate).amount(taxAmount).build();
    }

    private BigDecimal computeCustoms(BigDecimal priceAfterDiscount,
                                      String originCountry, String destCountry) {
        if (isSameCustomsZone(originCountry, destCountry)) return BigDecimal.ZERO;
        if (priceAfterDiscount.compareTo(taxConfiguration.getCustomsThreshold()) <= 0) return BigDecimal.ZERO;
        return priceAfterDiscount
                .multiply(taxConfiguration.getCustomsRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private ShippingBreakdown buildShippingBreakdown(Double weight, String countryCode,
                                                     BigDecimal priceAfterDiscount) {
        double w = (weight != null && weight > 0) ? weight : taxConfiguration.getDefaultWeight();
        BigDecimal baseCost;
        if      (w <= 0.5)  baseCost = taxConfiguration.getShippingSmall();
        else if (w <= 2.0)  baseCost = taxConfiguration.getShippingMedium();
        else if (w <= 10.0) baseCost = taxConfiguration.getShippingLarge();
        else                baseCost = taxConfiguration.getShippingXLarge();

        ShippingZone zone    = ShippingZone.fromCountryCode(countryCode);
        BigDecimal rawCost   = baseCost.multiply(zone.getMultiplier()).setScale(2, RoundingMode.HALF_UP);
        boolean isFree       = priceAfterDiscount.compareTo(taxConfiguration.getFreeShippingThreshold()) >= 0;
        BigDecimal finalCost = isFree ? BigDecimal.ZERO : rawCost;
        String freeReason    = isFree
                ? "Commande > " + taxConfiguration.getFreeShippingThreshold().toPlainString() + "€" : null;

        int[] est = taxConfiguration.getDeliveryEstimates().getOrDefault(zone.name(), new int[]{15, 30});
        LocalDate today = LocalDate.now();

        return ShippingBreakdown.builder()
                .zone(zone).multiplier(zone.getMultiplier()).baseCost(baseCost)
                .finalCost(finalCost).isFree(isFree).freeShippingReason(freeReason)
                .estimatedDeliveryMin(today.plusDays(est[0]).toString())
                .estimatedDeliveryMax(today.plusDays(est[1]).toString())
                .build();
    }

    private Double resolveWeight(Product product, ProductVariant variant) {
        if (variant != null && variant.getWeight() != null && variant.getWeight() > 0) return variant.getWeight();
        if (product.getWeight() != null && product.getWeight() > 0) return product.getWeight();
        return taxConfiguration.getDefaultWeight();
    }

    private int resolveStock(Product product, ProductVariant variant) {
        if (variant != null && variant.getStock() != null) return variant.getStock();
        return product.getStockQuantity() != null ? product.getStockQuantity() : 0;
    }

    private boolean isSameCustomsZone(String origin, String destination) {
        CountryTaxZone o = CountryTaxZone.fromCountryCode(origin);
        CountryTaxZone d = CountryTaxZone.fromCountryCode(destination);
        if (o.isEU() && d.isEU()) return true;
        return origin != null && origin.equalsIgnoreCase(destination);
    }
}
