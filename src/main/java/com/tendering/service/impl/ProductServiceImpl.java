package com.tendering.service.impl;

import com.tendering.dto.request.product.ProductCreateRequest;
import com.tendering.dto.request.product.ProductUpdateRequest;
import com.tendering.dto.response.product.ProductResponse;
import com.tendering.model.Product;
import com.tendering.repository.ProductRepository;
import com.tendering.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, UUID sellerPublicId) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                // set other fields when available (categoryId, brand, sellerPublicId etc.)
                .publicId(UUID.randomUUID())
                .build();

        productRepository.save(product);
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID publicId, ProductUpdateRequest request) {
        Product product = productRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        // update other fields here...
        product = productRepository.save(product);

        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID publicId) {
        Product product = productRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(String search, Long categoryId, String brand, Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByAuction(UUID auctionPublicId) {
        List<Product> products = productRepository.findByAuctionPublicId(auctionPublicId);
        return products.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse updateProductStatus(UUID publicId, String status) {
        Product product = productRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        // adjust to your domain type (e.g. enum). For now we accept String.
        product.setStatus(Product.ProductStatus.valueOf(status));
        product = productRepository.save(product);
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID publicId) {
        Product product = productRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductResponse addImageToProduct(UUID productPublicId, String imageUrl, boolean isPrimary) {
        // Simple implementation: if you have a ProductImage entity, create/save it and link to product.
        // For now: find product and pretend we added an image (persist if you change fields)
        Product product = productRepository.findByPublicId(productPublicId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // TODO: create ProductImage entity and save. As a placeholder, you could set a field or ignore.
        // e.g. product.addImage(new ProductImage(...));
        // For now, just save product to trigger any cascade if you change it:
        product = productRepository.save(product);

        return mapToResponse(product);
    }

    @Override
    @Transactional
    public void removeImageFromProduct(UUID productPublicId, Long imageId) {
        // TODO: implement removal of image entity by imageId and ensure product relationship is updated.
        // Placeholder: verify product exists then no-op
        productRepository.findByPublicId(productPublicId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        // delete logic for imageId goes here
    }

    @Override
    @Transactional
    public ProductResponse setPrimaryImage(UUID productPublicId, Long imageId) {
        // TODO: implement logic to set the image with imageId as primary for the product
        productRepository.findByPublicId(productPublicId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        // set primary logic here
        Product product = productRepository.findByPublicId(productPublicId).get();
        return mapToResponse(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .publicId(product.getPublicId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                // map other fields like images, status, sellerName when available
                .build();
    }
}