package com.ccnlthd.taskmanager.dto;

import com.ccnlthd.taskmanager.model.Category;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

/**
 * DTO tách hẳn khỏi {@link Category} để dùng cho module Admin.
 * Tránh lộ danh sách {@code tasks} (lazy) và thông tin nhạy cảm của User.
 */
@Getter
public class AdminCategoryDTO {
    private static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Long id;
    private final String name;
    private final Long ownerId;
    private final String ownerUsername;
    private final String formattedCreatedAt;

    public AdminCategoryDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.ownerId = category.getUser() == null ? null : category.getUser().getId();
        this.ownerUsername = category.getUser() == null ? "" : category.getUser().getUsername();
        this.formattedCreatedAt = category.getCreatedAt() == null
                ? ""
                : category.getCreatedAt().format(DATETIME_FORMAT);
    }
}
