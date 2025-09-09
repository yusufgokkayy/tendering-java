package com.tendering.controller;

import com.tendering.dto.common.ApiResponse;
import com.tendering.dto.request.product.ProductCreateRequest;
import com.tendering.dto.request.product.ProductUpdateRequest;
import com.tendering.dto.response.product.ProductResponse;
import com.tendering.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.tendering.security.CustomUserDetails;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

//    @PostMapping
//    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
//            @Valid @RequestBody ProductCreateRequest request,
//            @AuthenticationPrincipal CustomUserDetails currentUser) {
//
//        ProductResponse product = productService.createProduct(request, UUID.fromString(currentUser.getPublicId()));
//        return ResponseEntity.ok(ApiResponse.success("Ürün başarıyla oluşturuldu", product));
//    }
//
//    @GetMapping("/{productId}")
//    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable UUID productId) {
//        ProductResponse product = productService.getProductById(productId);
//        return ResponseEntity.ok(ApiResponse.success("Ürün bulundu", product));
//    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        throw new UnsupportedOperationException("createProduct henüz implemente edilmedi!");
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable UUID productId) {
        throw new UnsupportedOperationException("getProduct henüz implemente edilmedi!");
    }


    // Diğer endpoint'ler
}