package com.tendering.service;

import com.tendering.dto.request.category.CategoryCreateRequest;
import com.tendering.dto.request.category.CategoryUpdateRequest;
import com.tendering.dto.response.category.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    
    CategoryResponse createCategory(CategoryCreateRequest request);
    
    CategoryResponse updateCategory(Long id, CategoryUpdateRequest request);
    
    CategoryResponse getCategoryById(Long id);
    
    List<CategoryResponse> getAllCategories();
    
    List<CategoryResponse> getRootCategories();
    
    List<CategoryResponse> getSubcategories(Long parentId);
    
    Page<CategoryResponse> getCategoriesWithPagination(Pageable pageable);
    
    void deleteCategory(Long id);
    
    CategoryResponse updateDisplayOrder(Long id, Integer displayOrder);
}