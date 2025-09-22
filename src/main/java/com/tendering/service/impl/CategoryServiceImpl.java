package com.tendering.service.impl;

import com.tendering.dto.request.category.CategoryCreateRequest;
import com.tendering.dto.request.category.CategoryUpdateRequest;
import com.tendering.dto.response.category.CategoryResponse;
import com.tendering.exceptionHandlers.ResourceNotFoundException;
import com.tendering.model.Category;
import com.tendering.repository.CategoryRepository;
import com.tendering.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        log.debug("Creating new category: {}", request.getName());

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder())
                .build();

        // Set parent category if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent kategori bulunamadı: " + request.getParentId()));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        log.debug("Category created successfully with ID: {}", savedCategory.getId());

        return CategoryResponse.fromEntity(savedCategory);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        log.debug("Updating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı: " + id));

        // Update fields if provided
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        // Update parent category if provided
        if (request.getParentId() != null) {
            if (!request.getParentId().equals(id)) { // Prevent self-referencing
                Category parent = categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Parent kategori bulunamadı: " + request.getParentId()));
                category.setParent(parent);
            } else {
                throw new IllegalArgumentException("Kategori kendisinin alt kategorisi olamaz");
            }
        }

        Category updatedCategory = categoryRepository.save(category);
        log.debug("Category updated successfully with ID: {}", updatedCategory.getId());

        return CategoryResponse.fromEntity(updatedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        log.debug("Getting category by ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı: " + id));

        return CategoryResponse.fromEntityWithChildren(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.debug("Getting all categories");

        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        log.debug("Getting root categories");

        List<Category> rootCategories = categoryRepository.findByParentIsNullOrderByDisplayOrderAsc();
        return rootCategories.stream()
                .map(CategoryResponse::fromEntityWithChildren)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubcategories(Long parentId) {
        log.debug("Getting subcategories for parent ID: {}", parentId);

        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent kategori bulunamadı: " + parentId));

        List<Category> subcategories = categoryRepository.findByParentOrderByDisplayOrderAsc(parent);
        return subcategories.stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getCategoriesWithPagination(Pageable pageable) {
        log.debug("Getting categories with pagination: {}", pageable);

        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return categoryPage.map(CategoryResponse::fromEntity);
    }

    @Override
    public void deleteCategory(Long id) {
        log.debug("Deleting category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı: " + id));

        // Check if category has children
        if (!category.getChildren().isEmpty()) {
            throw new IllegalStateException("Alt kategorileri olan kategori silinemez. Önce alt kategorileri silin.");
        }

        categoryRepository.delete(category);
        log.debug("Category deleted successfully with ID: {}", id);
    }

    @Override
    public CategoryResponse updateDisplayOrder(Long id, Integer displayOrder) {
        log.debug("Updating display order for category ID: {} to: {}", id, displayOrder);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı: " + id));

        category.setDisplayOrder(displayOrder);
        Category updatedCategory = categoryRepository.save(category);

        log.debug("Display order updated successfully for category ID: {}", id);
        return CategoryResponse.fromEntity(updatedCategory);
    }
}