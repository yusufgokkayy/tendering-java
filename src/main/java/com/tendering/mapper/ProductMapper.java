package com.tendering.mapper;

import com.tendering.dto.request.product.ProductCreateRequest;
import com.tendering.dto.response.product.ProductResponse;
import com.tendering.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toProductResponse(Product product) {
        // Entity'den DTO'ya dönüşüm
        throw new UnsupportedOperationException("createProduct service henüz implemente edilmedi!");
    }

    public Product toProduct(ProductCreateRequest request) {
        // DTO'dan entity'ye dönüşüm
        throw new UnsupportedOperationException("createProduct service henüz implemente edilmedi!");
    }
}