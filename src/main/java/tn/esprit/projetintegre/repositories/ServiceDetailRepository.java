package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ServiceDetail;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceDetailRepository extends JpaRepository<ServiceDetail, Long> {

    @Override
    @EntityGraph(attributePaths = {"service"})
    Optional<ServiceDetail> findById(Long id);

    @EntityGraph(attributePaths = {"service"})
    List<ServiceDetail> findByServiceId(Long serviceId);

    @EntityGraph(attributePaths = {"service"})
    List<ServiceDetail> findByServiceIdAndIsActive(Long serviceId, Boolean isActive);
}
