package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.PageResponse;
import tn.esprit.projetintegre.dto.response.TransactionResponse;
import tn.esprit.projetintegre.entities.Transaction;
import tn.esprit.projetintegre.enums.TransactionType;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management APIs")
@CrossOrigin(origins = {"http://localhost:4200", "https://lively-wave-019e62b03.7.azurestaticapps.net"}, allowCredentials = "true")
public class TransactionController {

    private final TransactionService transactionService;
    private final DtoMapper dtoMapper;

    @GetMapping
    @Operation(summary = "Get all transactions paginated")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getAllTransactions(Pageable pageable) {
        Page<Transaction> page = transactionService.getAllTransactions(pageable);
        Page<TransactionResponse> response = page.map(dtoMapper::toTransactionResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toTransactionResponse(transaction)));
    }

    @GetMapping("/number/{transactionNumber}")
    @Operation(summary = "Get transaction by number")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByNumber(@PathVariable String transactionNumber) {
        Transaction transaction = transactionService.getTransactionByNumber(transactionNumber);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toTransactionResponse(transaction)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get transactions by user ID")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactionsByUserId(
            @PathVariable Long userId, Pageable pageable) {
        Page<Transaction> page = transactionService.getTransactionsByUserId(userId, pageable);
        Page<TransactionResponse> response = page.map(dtoMapper::toTransactionResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/wallet/{walletId}")
    @Operation(summary = "Get transactions by wallet ID")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactionsByWalletId(
            @PathVariable Long walletId, Pageable pageable) {
        Page<Transaction> page = transactionService.getTransactionsByWalletId(walletId, pageable);
        Page<TransactionResponse> response = page.map(dtoMapper::toTransactionResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get transactions by type")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactionsByType(
            @PathVariable TransactionType type,
            Pageable pageable) {
        Page<Transaction> page = transactionService.getTransactionsByType(type, pageable);
        Page<TransactionResponse> response = page.map(dtoMapper::toTransactionResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }
}
