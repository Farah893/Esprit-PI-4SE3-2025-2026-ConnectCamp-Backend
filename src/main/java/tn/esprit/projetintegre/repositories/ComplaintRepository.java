package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Complaint;
import tn.esprit.projetintegre.enums.ComplaintStatus;

import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "assignedTo"})
    Optional<Complaint> findById(Long id);

    @EntityGraph(attributePaths = {"user", "assignedTo"})
    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    @EntityGraph(attributePaths = {"user", "assignedTo"})
    Page<Complaint> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "assignedTo"})
    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "assignedTo"})
    Page<Complaint> findByAssignedToId(Long assignedToId, Pageable pageable);
}
