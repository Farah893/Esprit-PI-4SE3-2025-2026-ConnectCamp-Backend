package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Wallet;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.WalletRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletService {

    private final WalletRepository walletRepository;

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));
    }

    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user id: " + userId));
    }

    public BigDecimal getBalance(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        BigDecimal balance = wallet.getBalance();
        return balance != null ? balance : BigDecimal.ZERO;
    }

    public Wallet addFunds(Long userId, BigDecimal amount) {
        Wallet wallet = getWalletByUserId(userId);
        BigDecimal currentBalance = wallet.getBalance();
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
        wallet.setBalance(currentBalance.add(amount));
        return walletRepository.save(wallet);
    }

    public Wallet deductFunds(Long userId, BigDecimal amount) {
        Wallet wallet = getWalletByUserId(userId);
        BigDecimal currentBalance = wallet.getBalance();
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
        if (currentBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        wallet.setBalance(currentBalance.subtract(amount));
        return walletRepository.save(wallet);
    }

    public Wallet deactivateWallet(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setIsActive(false);
        return walletRepository.save(wallet);
    }

    public Wallet activateWallet(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setIsActive(true);
        return walletRepository.save(wallet);
    }
}
