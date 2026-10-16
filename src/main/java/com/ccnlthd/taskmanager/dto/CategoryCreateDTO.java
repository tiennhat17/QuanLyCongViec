package com.ccnlthd.taskmanager.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class CategoryCreateDTO {
    private Long id;

    @Size(min = 2, message = "Tên danh mục phải tối thiểu 2 ký tự")
    private String name;
}
