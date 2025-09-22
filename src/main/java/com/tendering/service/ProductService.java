package com.tendering.service;

import com.tendering.dto.request.product.ProductCreateRequest;
import com.tendering.dto.request.product.ProductUpdateRequest;
import com.tendering.dto.response.product.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest request, UUID sellerPublicId);

    ProductResponse updateProduct(UUID publicId, ProductUpdateRequest request);

    ProductResponse getProductById(UUID publicId);

    Page<ProductResponse> getAllProducts(String search, Long categoryId, String brand, Pageable pageable);

    Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);

    List<ProductResponse> getProductsByAuction(UUID auctionPublicId);

    ProductResponse updateProductStatus(UUID publicId, String status);

    void deleteProduct(UUID publicId);

    // Controller expects ProductResponse after adding an image, keep that consistent
    ProductResponse addImageToProduct(UUID productPublicId, String imageUrl, boolean isPrimary);

    // Controller passes image id as Long, accept Long here
    void removeImageFromProduct(UUID productPublicId, Long imageId);

    // Allow setting primary image by imageId (Long)
    ProductResponse setPrimaryImage(UUID productPublicId, Long imageId);
}