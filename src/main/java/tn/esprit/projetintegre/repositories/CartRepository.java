package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Cart;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Cart> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Cart> findById(Long id);

    // ── AJOUTÉ pour le scheduler ─────────────────────────────────────────────
    // Spring Data JPA génère : SELECT * FROM carts WHERE updated_at < :cutoffTime
    // Utilisé pour trouver les paniers abandonnés (non modifiés depuis X jours)
    List<Cart> findByUpdatedAtBefore(LocalDateTime cutoffTime);
}