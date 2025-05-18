package com.akida.ecommerce.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CategoryHierarchyDto {
    private Long id;
    private String name;
    private String description;
    private String imagePath;
    private List<CategoryHierarchyDto> children;

}
