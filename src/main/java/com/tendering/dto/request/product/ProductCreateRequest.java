package com.tendering.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    @NotBlank(message = "Ürün adı boş olamaz")
    private String name;

    @Size(max = 5000, message = "Açıklama en fazla 5000 karakter olabilir")
    private String description;

    private String brand;

    @NotNull(message = "Kategori ID boş olamaz")
    private Long categoryId;

    @NotNull(message = "Başlangıç fiyatı belirtilmelidir")
    @Positive(message = "Başlangıç fiyatı pozitif olmalıdır")
    private BigDecimal startingPrice;

    private BigDecimal reservePrice;

    private UUID auctionId;
}