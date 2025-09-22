package com.tendering.dto.response.category;

import com.tendering.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    private String imageUrl;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CategoryResponse> children;
    private int childrenCount;

    public static CategoryResponse fromEntity(Category category) {
        return fromEntity(category, false);
    }

    public static CategoryResponse fromEntity(Category category, boolean includeChildren) {
        CategoryResponseBuilder builder = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .imageUrl(category.getImageUrl())
                .displayOrder(category.getDisplayOrder())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .childrenCount(category.getChildren() != null ? category.getChildren().size() : 0);

        if (includeChildren && category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<CategoryResponse> children = category.getChildren().stream()
                    .map(child -> CategoryResponse.fromEntity(child, false))
                    .collect(Collectors.toList());
            builder.children(children);
        }

        return builder.build();
    }

    public static CategoryResponse fromEntityWithChildren(Category category) {
        return fromEntity(category, true);
    }
}