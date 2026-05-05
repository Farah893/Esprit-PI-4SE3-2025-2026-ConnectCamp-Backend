package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.ConditionPromotion;
import tn.esprit.projetintegre.enums.TypeCondition;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConditionPromotionRepository extends JpaRepository<ConditionPromotion, Long> {

    @Override
    @EntityGraph(attributePaths = {"promotion"}) // Charge la promotion associée
    Optional<ConditionPromotion> findById(Long id);

    @EntityGraph(attributePaths = {"promotion"})
    List<ConditionPromotion> findByPromotionId(Long promotionId);

    @EntityGraph(attributePaths = {"promotion"})
    List<ConditionPromotion> findByTypeCondition(TypeCondition type);
}
