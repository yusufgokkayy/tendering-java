package com.tendering.dto.request.category;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {

    @Size(max = 255, message = "Kategori adı en fazla 255 karakter olabilir")
    private String name;

    @Size(max = 1000, message = "Açıklama en fazla 1000 karakter olabilir")
    private String description;

    private Long parentId;

    @Size(max = 500, message = "Resim URL'i en fazla 500 karakter olabilir")
    private String imageUrl;

    private Integer displayOrder;
}