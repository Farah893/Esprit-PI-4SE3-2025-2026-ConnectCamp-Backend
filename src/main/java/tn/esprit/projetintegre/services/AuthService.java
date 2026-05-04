package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.dto.AuthRequest;
import tn.esprit.projetintegre.dto.AuthResponse;
import tn.esprit.projetintegre.dto.RegisterRequest;
import tn.esprit.projetintegre.entities.Cart;
import tn.esprit.projetintegre.entities.Organizer;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.entities.Wallet;
import tn.esprit.projetintegre.enums.Role;
import tn.esprit.projetintegre.enums.SponsorStatus;
import tn.esprit.projetintegre.repositories.CartRepository;
import tn.esprit.projetintegre.repositories.OrganizerRepository;
import tn.esprit.projetintegre.repositories.UserRepository;
import tn.esprit.projetintegre.repositories.WalletRepository;
import tn.esprit.projetintegre.security.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final WalletRepository walletRepository;
    private final OrganizerRepository organizerRepository; // added
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JavaMailSender mailSender;
    @Value("${app.auth.reset-code-expiration-minutes:10}")
    private int resetCodeExpirationMinutes;
    @Value("${spring.mail.username}")
    private String resetCodeSender;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already taken");
        }

        // Create user
        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setCountry(request.getCountry());
        user.setAge(request.getAge());

        // Set role – use provided or default to USER
        Role role = request.getRole() != null ? request.getRole() : Role.USER;
        user.setRole(role);
        user.setIsActive(true);
        
        // Handle seller/buyer flags with explicit defaults
        boolean isSellerFlag = Boolean.TRUE.equals(request.getIsSeller()) || role == Role.SELLER;
        boolean isBuyerFlag = request.getIsBuyer() == null || Boolean.TRUE.equals(request.getIsBuyer());
        
        user.setIsSeller(isSellerFlag);
        user.setIsBuyer(isBuyerFlag);
        user.setStoreName(request.getStoreName());
        user.setBio(request.getBio());

        if (role == Role.SPONSOR) {
            user.setSponsorStatus(SponsorStatus.PENDING);
        }
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.saveAndFlush(user); // ensure ID generated

        // Create cart for the new user
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        // Create wallet for the new user
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency("TND");
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        wallet.setIsActive(true);
        walletRepository.save(wallet);

        // If the user is an organizer, create an associated organizer record
        if (role == Role.ORGANIZER) {
            Organizer organizer = new Organizer();
            organizer.setUser(user);
            organizer.setCompanyName(user.getName() + "'s Organization");
            organizer.setVerified(true);
            organizer.setActive(true);
            organizer.setCreatedAt(LocalDateTime.now());
            organizer.setUpdatedAt(LocalDateTime.now());
            organizerRepository.save(organizer);
        }

        // Generate JWT directly from the persisted username.
        // Avoiding an immediate authentication step prevents transaction rollback
        // in case AuthenticationManager fails for transient security-context reasons.
        String token = jwtTokenProvider.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .organizerId(ensureOrganizerId(user))
                .isSeller(user.getIsSeller())
                .isBuyer(user.getIsBuyer())
                .storeName(user.getStoreName())
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .organizerId(ensureOrganizerId(user))
                .isSeller(user.getIsSeller())
                .isBuyer(user.getIsBuyer())
                .storeName(user.getStoreName())
                .build();
    }

    @Transactional
    public Map<String, String> requestPasswordResetCode(String email) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "sent");

        User user = userRepository.findByEmail(email.trim())
                .orElse(null);

        if (user != null) {
            String resetCode = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
            user.setPasswordResetToken(resetCode);
            user.setPasswordResetExpires(LocalDateTime.now().plusMinutes(resetCodeExpirationMinutes));
            userRepository.save(user);
            sendResetCodeEmail(user.getEmail(), resetCode);
        } else {
            log.info("Password reset requested for non-existent email: {}", maskEmail(email));
        }

        return response;
    }

    @Transactional
    public void resetPasswordWithCode(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or verification code"));

        if (user.getPasswordResetToken() == null || user.getPasswordResetExpires() == null) {
            throw new IllegalArgumentException("Verification code has not been requested");
        }
        if (user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code has expired");
        }
        if (!user.getPasswordResetToken().equals(code.trim())) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void sendResetCodeEmail(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(resetCodeSender);
            message.setTo(toEmail);
            message.setSubject("Your Password Reset Code");
            message.setText("Your verification code is: " + code + "\n\nThis code expires in "
                    + resetCodeExpirationMinutes + " minutes.");
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send password reset email to {}", toEmail, ex);
            throw new IllegalStateException("Unable to send verification email. Please try again later.");
        }
    }

    private Long ensureOrganizerId(User user) {
        if (user == null || user.getId() == null || user.getRole() != Role.ORGANIZER) {
            return null;
        }

        return organizerRepository.findByUser_Id(user.getId())
                .orElseGet(() -> organizerRepository.save(Organizer.builder()
                        .user(user)
                        .companyName(user.getName() + "'s Organization")
                        .verified(true)
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()))
                .getId();
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "<empty>";
        }

        String normalized = email.trim();
        int atIndex = normalized.indexOf('@');
        if (atIndex <= 1) {
            return normalized;
        }

        return normalized.charAt(0) + "***" + normalized.substring(atIndex);
    }
}