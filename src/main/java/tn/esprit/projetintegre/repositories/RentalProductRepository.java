package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.RentalProduct;
import tn.esprit.projetintegre.enums.AvailabilityStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalProductRepository extends JpaRepository<RentalProduct, Long> {

    @Override
    @EntityGraph(attributePaths = {"rental", "product"})
    Optional<RentalProduct> findById(Long id);

    @EntityGraph(attributePaths = {"rental", "product"})
    List<RentalProduct> findByRentalId(Long rentalId);

    @EntityGraph(attributePaths = {"rental", "product"})
    List<RentalProduct> findByProductId(Long productId);

    @EntityGraph(attributePaths = {"rental", "product"})
    List<RentalProduct> findByStatus(AvailabilityStatus status);
}
