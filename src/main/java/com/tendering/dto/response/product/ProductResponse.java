package com.tendering.dto.response.product;

import com.tendering.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private UUID publicId;
    private String name;
    private String description;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private BigDecimal startingPrice;
    private BigDecimal reservePrice;
    private UUID sellerPublicId;
    private String sellerName;
    private UUID auctionPublicId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductImageResponse> images;

    public static ProductResponse fromEntity(Product product) {
        return ProductResponse.builder()
                .publicId(product.getPublicId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .startingPrice(product.getStartingPrice())
                .reservePrice(product.getReservePrice())
                .sellerPublicId(product.getSeller().getPublicId())
                .sellerName(product.getSeller().getFullName())
                .auctionPublicId(product.getAuction() != null ? product.getAuction().getPublicId() : null)
                .status(product.getStatus().name())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .images(product.getImages().stream()
                        .map(ProductImageResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}