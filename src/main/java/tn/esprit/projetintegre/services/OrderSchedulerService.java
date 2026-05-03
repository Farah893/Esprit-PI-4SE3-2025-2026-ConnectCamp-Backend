package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Cart;
import tn.esprit.projetintegre.entities.Order;
import tn.esprit.projetintegre.enums.OrderStatus;
import tn.esprit.projetintegre.enums.PaymentStatus;
import tn.esprit.projetintegre.repositories.CartRepository;
import tn.esprit.projetintegre.repositories.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * OrderSchedulerService — Tâches planifiées de maintenance des commandes
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Nécessite @EnableScheduling sur ProjetIntegreApplication (déjà ajouté).
 *
 * TÂCHES :
 *  1. autoCancelStaleOrders()  — toutes les 5 min  — annule les commandes PENDING
 *                                                    non payées depuis plus de 24h
 *  2. cleanUpAbandonedCarts()  — tous les jours à 3h — supprime les paniers
 *                                                    inactifs depuis plus de 7 jours
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSchedulerService {

    private final OrderRepository orderRepository;
    private final CartRepository  cartRepository;

    // ═══════════════════════════════════════════════════════════════════════════
    // TÂCHE 1 — Annulation automatique des commandes en attente expirées
    // ═══════════════════════════════════════════════════════════════════════════
    //
    // fixedRate = 300_000 ms = toutes les 5 minutes
    //
    // Règle métier :
    //   si une commande est PENDING + paiement PENDING
    //   ET créée il y a plus de 24 heures
    //   → elle est annulée automatiquement
    // ═══════════════════════════════════════════════════════════════════════════
    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void autoCancelStaleOrders() {
        log.info("[SCHEDULER] ▶ autoCancelStaleOrders — démarrage");

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        List<Order> staleOrders = orderRepository
                .findByStatusAndPaymentStatusAndCreatedAtBefore(
                        OrderStatus.PENDING,
                        PaymentStatus.PENDING,
                        cutoff
                );

        if (staleOrders.isEmpty()) {
            log.info("[SCHEDULER] ✔ Aucune commande périmée trouvée.");
            return;
        }

        int count = 0;
        for (Order order : staleOrders) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());
            orderRepository.save(order);
            count++;

            log.info("[SCHEDULER] ✘ Commande annulée : {} (créée le {})",
                    order.getOrderNumber(), order.getCreatedAt());
        }

        log.info("[SCHEDULER] ✔ autoCancelStaleOrders terminé — {} commande(s) annulée(s).", count);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TÂCHE 2 — Nettoyage des paniers abandonnés
    // ═══════════════════════════════════════════════════════════════════════════
    //
    // cron = "0 0 3 * * ?" → tous les jours à 03h00 (heure serveur)
    // Format cron Spring : seconde minute heure jour mois jour-semaine
    //
    // Règle métier :
    //   si un panier n'a pas été mis à jour depuis plus de 7 jours
    //   → il est supprimé (CascadeType.ALL supprime aussi ses CartItems)
    // ═══════════════════════════════════════════════════════════════════════════
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanUpAbandonedCarts() {
        log.info("[SCHEDULER] ▶ cleanUpAbandonedCarts — démarrage");

        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);

        List<Cart> abandonedCarts = cartRepository.findByUpdatedAtBefore(cutoff);

        if (abandonedCarts.isEmpty()) {
            log.info("[SCHEDULER] ✔ Aucun panier abandonné trouvé.");
            return;
        }

        int count = abandonedCarts.size();
        cartRepository.deleteAll(abandonedCarts);

        log.info("[SCHEDULER] ✔ cleanUpAbandonedCarts terminé — {} panier(s) supprimé(s).", count);
    }
}