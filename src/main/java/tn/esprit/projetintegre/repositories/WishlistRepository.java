package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Wishlist;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Wishlist> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    Optional<Wishlist> findByUserIdAndName(Long userId, String name);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    Optional<Wishlist> findById(Long id);
}
