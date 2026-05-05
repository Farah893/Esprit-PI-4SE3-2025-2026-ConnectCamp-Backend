package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ShippingAddress;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<ShippingAddress> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    List<ShippingAddress> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<ShippingAddress> findByUserIdAndIsActive(Long userId, Boolean isActive);

    @EntityGraph(attributePaths = {"user"})
    Optional<ShippingAddress> findByUserIdAndIsDefault(Long userId, Boolean isDefault);

    @EntityGraph(attributePaths = {"user"})
    List<ShippingAddress> findByCity(String city);

    @EntityGraph(attributePaths = {"user"})
    List<ShippingAddress> findByCountry(String country);
}
