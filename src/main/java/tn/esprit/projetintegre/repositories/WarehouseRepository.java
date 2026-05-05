package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Warehouse;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    @EntityGraph(attributePaths = {"manager"})
    Optional<Warehouse> findByCode(String code);

    @EntityGraph(attributePaths = {"manager"})
    List<Warehouse> findByIsActive(Boolean isActive);

    @EntityGraph(attributePaths = {"manager"})
    List<Warehouse> findByIsPrimary(Boolean isPrimary);

    @EntityGraph(attributePaths = {"manager"})
    List<Warehouse> findByCity(String city);

    @EntityGraph(attributePaths = {"manager"})
    List<Warehouse> findByCountry(String country);

    @EntityGraph(attributePaths = {"manager"})
    List<Warehouse> findByManagerId(Long managerId);

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"manager"})
    Optional<Warehouse> findById(Long id);
}
