package com.tendering.dto.request.product;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {
    private String name;

    @Size(max = 5000, message = "Açıklama en fazla 5000 karakter olabilir")
    private String description;

    private String brand;

    private Long categoryId;

    @Positive(message = "Başlangıç fiyatı pozitif olmalıdır")
    private BigDecimal startingPrice;

    private BigDecimal reservePrice;
}