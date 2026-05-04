package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.AuthRequest;
import tn.esprit.projetintegre.dto.AuthResponse;
import tn.esprit.projetintegre.dto.ForgotPasswordRequest;
import tn.esprit.projetintegre.dto.RegisterRequest;
import tn.esprit.projetintegre.dto.ResetPasswordRequest;
import tn.esprit.projetintegre.services.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with username/email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset verification code by email")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        Map<String, String> response = authService.requestPasswordResetCode(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "If this email exists, a verification code has been sent.",
                response
        ));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password after email code verification")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPasswordWithCode(request.getEmail(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
    }
}