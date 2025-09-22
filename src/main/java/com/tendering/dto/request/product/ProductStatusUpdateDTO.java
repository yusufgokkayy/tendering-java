package com.tendering.dto.request.product;

import com.tendering.model.Product;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusUpdateDTO {
    @NotNull(message = "Durum belirtilmelidir")
    private Product.ProductStatus status;
}