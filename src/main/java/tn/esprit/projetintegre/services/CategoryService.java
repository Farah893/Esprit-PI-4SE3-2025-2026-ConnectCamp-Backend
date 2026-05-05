package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.dto.response.CategorySalesReportResponse;
import tn.esprit.projetintegre.entities.Category;
import tn.esprit.projetintegre.enums.OrderStatus;
import tn.esprit.projetintegre.exception.DuplicateResourceException;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Ajout de @Transactional pour garder la session ouverte pour le DtoMapper
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIsNull();
    }

    @Transactional(readOnly = true)
    public List<Category> getSubcategories(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
    }

    @Transactional
    public Category createCategory(Category category, Long parentId) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new DuplicateResourceException("Category with this name already exists");
        }

        if (parentId != null) {
            Category parent = getCategoryById(parentId);
            category.setParent(parent);
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);

        if (categoryDetails.getName() != null) {
            if (!category.getName().equals(categoryDetails.getName()) &&
                    categoryRepository.existsByName(categoryDetails.getName())) {
                throw new DuplicateResourceException("Category with this name already exists");
            }
            category.setName(categoryDetails.getName());
        }
        if (categoryDetails.getDescription() != null) category.setDescription(categoryDetails.getDescription());
        if (categoryDetails.getImage() != null) category.setImage(categoryDetails.getImage());
        if (categoryDetails.getDisplayOrder() != null) category.setDisplayOrder(categoryDetails.getDisplayOrder());
        if (categoryDetails.getIsActive() != null) category.setIsActive(categoryDetails.getIsActive());

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        category.setIsActive(false);
        categoryRepository.save(category);
    }
    // Ajouter dans CategoryService.java :



    /**
     * Rapport de ventes par catégorie.
     * Délègue à la requête JPQL multi-tables du repository.
     *
     * @param status filtre sur le statut de commande (ex: DELIVERED)
     * @return liste triée par revenu décroissant
     */
    @Transactional(readOnly = true)
    public List<CategorySalesReportResponse> getCategorySalesReport(OrderStatus status) {
        OrderStatus effectiveStatus = (status != null) ? status : OrderStatus.DELIVERED;
        return categoryRepository.getCategorySalesReport(effectiveStatus);
    }
}
