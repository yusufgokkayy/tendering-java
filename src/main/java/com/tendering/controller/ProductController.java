package com.tendering.controller;

import com.tendering.dto.request.product.ProductCreateRequest;
import com.tendering.dto.response.product.ProductResponse;
import com.tendering.dto.request.product.ProductStatusUpdateDTO;
import com.tendering.dto.request.product.ProductUpdateRequest;
import com.tendering.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
//    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest productCreateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        // UserDetails'den kullanıcı UUID'sini alacağımızı varsayıyorum
        if (userDetails == null) {
            throw new RuntimeException("User is not authenticated!");
        }
        UUID sellerPublicId = UUID.fromString(userDetails.getUsername());
        ProductResponse createdProduct = productService.createProduct(productCreateRequest, sellerPublicId);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID publicId) {
        ProductResponse product = productService.getProductById(publicId);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        Page<ProductResponse> products = productService.getAllProducts(search, categoryId, brand, pageable);
        return ResponseEntity.ok(products);
    }


    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProductResponse> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/auction/{auctionPublicId}")
    public ResponseEntity<List<ProductResponse>> getProductsByAuction(@PathVariable UUID auctionPublicId) {
        List<ProductResponse> products = productService.getProductsByAuction(auctionPublicId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID publicId,
            @Valid @RequestBody ProductUpdateRequest productUpdateRequest) {
        ProductResponse updatedProduct = productService.updateProduct(publicId, productUpdateRequest);
        return ResponseEntity.ok(updatedProduct);
    }

    @PatchMapping("/{publicId}/status")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProductStatus(
            @PathVariable UUID publicId,
            @Valid @RequestBody ProductStatusUpdateDTO statusUpdateDTO) {
        ProductResponse updatedProduct = productService.updateProductStatus(publicId, String.valueOf(statusUpdateDTO.getStatus()));
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID publicId) {
        productService.deleteProduct(publicId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{publicId}/images")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> addImageToProduct(
            @PathVariable UUID publicId,
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "isPrimary", defaultValue = "false") boolean isPrimary) {
        // Normalde burada dosya yükleme işlemi yapılacak ve URL alınacak
        // Şimdilik basit tutmak için direkt URL alıyormuş gibi yapıyoruz
        String imageUrl = "http://example.com/images/" + System.currentTimeMillis() + ".jpg";

        ProductResponse updatedProduct = productService.addImageToProduct(publicId, imageUrl, isPrimary);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{publicId}/images/{imageId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Void> removeImageFromProduct(
            @PathVariable UUID publicId,
            @PathVariable Long imageId) {
        productService.removeImageFromProduct(publicId, imageId);
        return ResponseEntity.noContent().build();
    }
}