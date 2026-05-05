package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ServiceApplication;
import tn.esprit.projetintegre.enums.ServiceApplicationStatus;

import java.util.Optional;

@Repository
public interface ServiceApplicationRepository extends JpaRepository<ServiceApplication, Long> {

    @Override
    @EntityGraph(attributePaths = {"applicant", "service"})
    Optional<ServiceApplication> findById(Long id);

    @EntityGraph(attributePaths = {"applicant", "service"})
    Optional<ServiceApplication> findByApplicationNumber(String applicationNumber);

    @EntityGraph(attributePaths = {"applicant", "service"})
    Page<ServiceApplication> findByApplicantId(Long applicantId, Pageable pageable);

    @EntityGraph(attributePaths = {"applicant", "service"})
    Page<ServiceApplication> findByServiceId(Long serviceId, Pageable pageable);

    @EntityGraph(attributePaths = {"applicant", "service"})
    Page<ServiceApplication> findByStatus(ServiceApplicationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"applicant", "service"})
    @Query("SELECT sa FROM ServiceApplication sa WHERE sa.applicant.id = :userId AND sa.service.id = :serviceId")
    Optional<ServiceApplication> findByApplicantIdAndServiceId(@Param("userId") Long userId, @Param("serviceId") Long serviceId);

    @EntityGraph(attributePaths = {"applicant", "service"})
    @Query("SELECT sa FROM ServiceApplication sa WHERE sa.service.id = :serviceId AND sa.status = :status")
    Page<ServiceApplication> findByServiceIdAndStatus(@Param("serviceId") Long serviceId, @Param("status") ServiceApplicationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"applicant", "service"})
    @Query("SELECT COUNT(sa) FROM ServiceApplication sa WHERE sa.service.id = :serviceId AND sa.status = :status")
    Long countByServiceIdAndStatus(@Param("serviceId") Long serviceId, @Param("status") ServiceApplicationStatus status);

    @EntityGraph(attributePaths = {"applicant", "service"})
    @Query("SELECT sa FROM ServiceApplication sa WHERE sa.status = 'PENDING' ORDER BY sa.appliedAt ASC")
    Page<ServiceApplication> findPendingApplications(Pageable pageable);
}
