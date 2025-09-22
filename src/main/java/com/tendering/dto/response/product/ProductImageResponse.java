package com.tendering.dto.response.product;

import com.tendering.model.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {
    private Long id;
    private UUID productPublicId;
    private String url;
    private boolean isPrimary;

    public static ProductImageResponse fromEntity(ProductImage image) {
        return ProductImageResponse.builder()
                .id(image.getId())
                .productPublicId(image.getProduct().getPublicId())
                .url(image.getImageUrl())
                .isPrimary(image.getIsPrimary())
                .build();
    }
}