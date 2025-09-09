package com.tendering.repository;

import com.tendering.model.Auction;
import com.tendering.model.Category;
import com.tendering.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByPublicId(UUID publicId);

    List<Product> findByAuction(Auction auction);

    Page<Product> findByCategory(Category category, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    Page<Product> findByBrandContainingIgnoreCase(String brand, Pageable pageable);

    Page<Product> findByCategoryAndNameContainingIgnoreCase(Category category, String name, Pageable pageable);
}