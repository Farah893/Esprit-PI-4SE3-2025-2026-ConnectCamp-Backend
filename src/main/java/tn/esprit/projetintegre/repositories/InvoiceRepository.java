package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Invoice;
import tn.esprit.projetintegre.enums.PaymentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "order"})
    Optional<Invoice> findById(Long id);

    @EntityGraph(attributePaths = {"user", "order"})
    Optional<Invoice> findByInvoiceNumber(String number);

    @EntityGraph(attributePaths = {"user", "order"})
    List<Invoice> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "order"})
    Page<Invoice> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "order"})
    List<Invoice> findByStatus(PaymentStatus status);

    @EntityGraph(attributePaths = {"user", "order"})
    List<Invoice> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {"user", "order"})
    @Query("SELECT i FROM Invoice i WHERE i.dueDate <= :date AND i.status = 'PENDING'")
    List<Invoice> findOverdueInvoices(LocalDate date);

    /**
     * Compte les factures d'un mois donné pour générer le numéro séquentiel
     * Exemple: countByMonth("2026", "05") → 3 → prochain sera FAC-2026-05-00004
     */
    @Query("SELECT COUNT(i) FROM Invoice i " +
            "WHERE FUNCTION('YEAR', i.issueDate) = :year " +
            "AND FUNCTION('MONTH', i.issueDate) = :month")
    long countByMonth(@Param("year") int year, @Param("month") int month);
}
