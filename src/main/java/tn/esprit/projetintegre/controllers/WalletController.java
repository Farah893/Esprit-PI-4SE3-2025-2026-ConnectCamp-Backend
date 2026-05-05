package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.response.WalletResponse;
import tn.esprit.projetintegre.entities.Wallet;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.WalletService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets", description = "Wallet management APIs")
@CrossOrigin(origins = {"http://localhost:4200", "https://lively-wave-019e62b03.7.azurestaticapps.net"}, allowCredentials = "true")
public class WalletController {

    private final WalletService walletService;
    private final DtoMapper dtoMapper;

    @GetMapping
    @Operation(summary = "Get all wallets")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getAllWallets() {
        List<Wallet> wallets = walletService.getAllWallets();
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toWalletResponseList(wallets)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wallet by ID")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletById(@PathVariable Long id) {
        Wallet wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toWalletResponse(wallet)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get wallet by user ID")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletByUserId(@PathVariable Long userId) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toWalletResponse(wallet)));
    }

    @GetMapping("/user/{userId}/balance")
    @Operation(summary = "Get wallet balance by user ID")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getBalance(userId)));
    }

    @PostMapping("/user/{userId}/add-funds")
    @Operation(summary = "Add funds to wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> addFunds(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount) {
        Wallet wallet = walletService.addFunds(userId, amount);
        return ResponseEntity.ok(ApiResponse.success("Funds added successfully", dtoMapper.toWalletResponse(wallet)));
    }

    @PostMapping("/user/{userId}/deduct-funds")
    @Operation(summary = "Deduct funds from wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> deductFunds(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount) {
        Wallet wallet = walletService.deductFunds(userId, amount);
        return ResponseEntity.ok(ApiResponse.success("Funds deducted successfully", dtoMapper.toWalletResponse(wallet)));
    }

    @PutMapping("/user/{userId}/deactivate")
    @Operation(summary = "Deactivate wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> deactivateWallet(@PathVariable Long userId) {
        Wallet wallet = walletService.deactivateWallet(userId);
        return ResponseEntity.ok(ApiResponse.success("Wallet deactivated successfully", dtoMapper.toWalletResponse(wallet)));
    }

    @PutMapping("/user/{userId}/activate")
    @Operation(summary = "Activate wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> activateWallet(@PathVariable Long userId) {
        Wallet wallet = walletService.activateWallet(userId);
        return ResponseEntity.ok(ApiResponse.success("Wallet activated successfully", dtoMapper.toWalletResponse(wallet)));
    }
}
