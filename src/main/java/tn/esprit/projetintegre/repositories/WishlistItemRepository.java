package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.WishlistItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    @EntityGraph(attributePaths = {"wishlist", "product"})
    List<WishlistItem> findByWishlistId(Long wishlistId);

    @EntityGraph(attributePaths = {"wishlist", "product"})
    Optional<WishlistItem> findByWishlistIdAndProductId(Long wishlistId, Long productId);

    void deleteByWishlistIdAndProductId(Long wishlistId, Long productId);

    @EntityGraph(attributePaths = {"wishlist", "product"})
    List<WishlistItem> findByNotifyOnPriceDrop(Boolean notify);

    @EntityGraph(attributePaths = {"wishlist", "product"})
    List<WishlistItem> findByNotifyOnBackInStock(Boolean notify);

    @EntityGraph(attributePaths = {"wishlist", "product"})
    Optional<WishlistItem> findById(Long id);
}
