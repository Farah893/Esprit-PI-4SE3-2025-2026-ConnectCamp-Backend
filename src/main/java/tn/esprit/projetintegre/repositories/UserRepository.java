package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.enums.Role;
import tn.esprit.projetintegre.enums.SponsorStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Note: On n'ajoute pas d'EntityGraph ici si User n'a pas de relations lourdes à charger par défaut
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByRoleAndSponsorStatus(Role role, SponsorStatus sponsorStatus);
    List<User> findBySponsorStatus(SponsorStatus sponsorStatus);
    Page<User> findByIsActiveTrue(Pageable pageable);
    Page<User> findByIsSellerTrue(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isSeller = true AND u.sellerVerified = true")
    List<User> findVerifiedSellers();

    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(String keyword, Pageable pageable);
}
