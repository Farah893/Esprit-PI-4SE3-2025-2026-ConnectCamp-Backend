package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.CartItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @EntityGraph(attributePaths = {"cart", "product"}) // Charge les relations nécessaires
    Optional<CartItem> findById(Long id);

    @EntityGraph(attributePaths = {"cart", "product"})
    List<CartItem> findByCartId(Long cartId);

    @EntityGraph(attributePaths = {"cart", "product"})
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    @EntityGraph(attributePaths = {"cart", "product"})
    void deleteByCartId(Long cartId);
}
