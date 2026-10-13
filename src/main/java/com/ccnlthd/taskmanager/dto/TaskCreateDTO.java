
package com.ccnlthd.taskmanager.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter @NoArgsConstructor 
public class TaskCreateDTO {

    private Long id;

    @Size(
        min = 2,
        max = 200,
        message = "Tiêu đề phải từ 2 đến 200 ký tự"
    )
    private String title;

    private String description;

    private LocalDate dueDate;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;
}
