package com.tendering.repository;

import com.tendering.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findByParentIsNullOrderByDisplayOrderAsc();

    List<Category> findByParentOrderByDisplayOrderAsc(Category parent);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    List<Category> findRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parent = :parent")
    List<Category> findSubcategories(Category parent);
}