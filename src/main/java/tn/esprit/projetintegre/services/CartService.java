package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Cart;
import tn.esprit.projetintegre.entities.CartItem;
import tn.esprit.projetintegre.entities.Product;
import tn.esprit.projetintegre.entities.Pack;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.CartItemRepository;
import tn.esprit.projetintegre.repositories.CartRepository;
import tn.esprit.projetintegre.repositories.ProductRepository;
import tn.esprit.projetintegre.repositories.PackRepository;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final PackRepository packRepository;
    private final UserService userService ;


    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userService.getUserById(userId); // inject UserService
                    Cart newCart = Cart.builder()
                            .user(user)
                            .totalAmount(BigDecimal.ZERO)
                            .discountAmount(BigDecimal.ZERO)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public Cart addItemToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = getCartByUserId(userId);
        
        // Try to find as Product first, then as Pack
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Pack> packOpt = productOpt.isPresent() ? Optional.empty() : packRepository.findById(productId);

        if (productOpt.isEmpty() && packOpt.isEmpty()) {
            throw new ResourceNotFoundException("Product or Pack not found with ID: " + productId);
        }

        Optional<CartItem> existingItem;
        if (productOpt.isPresent()) {
            existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        } else {
            // For packs, we need a separate find method or manual check
            existingItem = cart.getItems().stream()
                    .filter(i -> i.getPack() != null && i.getPack().getId().equals(productId))
                    .findFirst();
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem.CartItemBuilder itemBuilder = CartItem.builder()
                    .cart(cart)
                    .quantity(quantity);
            
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                BigDecimal price = product.getPrice();
                if (price == null) price = BigDecimal.ZERO;
                itemBuilder.product(product).price(price);
            } else {
                Pack pack = packOpt.get();
                BigDecimal price = pack.getPrice();
                if (price == null) price = BigDecimal.ZERO;
                itemBuilder.pack(pack).price(price);
            }
            
            CartItem newItem = itemBuilder.build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        // Force subtotal recalculation with DB prices
        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateCartItemQuantity(Long userId, Long itemId, Integer quantity) {
        Cart cart = getCartByUserId(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItemFromCart(Long userId, Long itemId) {
        Cart cart = getCartByUserId(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().clear();
        cartItemRepository.deleteByCartId(cart.getId());
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setAppliedCouponCode(null);
        return cartRepository.save(cart);
    }
}
