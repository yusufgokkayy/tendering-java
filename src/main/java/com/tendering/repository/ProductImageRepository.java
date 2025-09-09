package com.tendering.repository;

import com.tendering.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductId(Long productId);

    List<ProductImage> findByProductPublicId(UUID productPublicId);

    Optional<ProductImage> findByPublicId(UUID publicId);

    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);

    // Bu metodu düzelttim (isMainImage -> isPrimary)
    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(Long productId);
}