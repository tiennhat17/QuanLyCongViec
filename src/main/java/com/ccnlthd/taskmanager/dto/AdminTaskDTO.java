package com.ccnlthd.taskmanager.dto;

import com.ccnlthd.taskmanager.model.Task;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

/**
 * DTO tách hẳn khỏi {@link Task} để dùng cho module Admin.
 * Chỉ phơi ra những field cần hiển thị, không lộ quan hệ JPA bên trong.
 */
@Getter
public class AdminTaskDTO {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Long id;
    private final String title;
    private final String status;
    private final String formattedDueDate;
    private final boolean overdue;
    private final boolean completed;
    private final Long ownerId;
    private final String ownerUsername;
    private final Long categoryId;
    private final String categoryName;
    private final String formattedCreatedAt;

    public AdminTaskDTO(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.status = task.getStatus() == null ? "" : task.getStatus().name();
        this.formattedDueDate = task.getDueDate() == null
                ? "Chưa đặt hạn"
                : task.getDueDate().format(DATE_FORMAT);
        this.overdue = task.isOverdue();
        this.completed = task.isCompleted();
        this.ownerId = task.getUser() == null ? null : task.getUser().getId();
        this.ownerUsername = task.getUser() == null ? "" : task.getUser().getUsername();
        this.categoryId = task.getCategory() == null ? null : task.getCategory().getId();
        this.categoryName = task.getCategory() == null ? "" : task.getCategory().getName();
        this.formattedCreatedAt = task.getCreatedAt() == null
                ? ""
                : task.getCreatedAt().format(DATETIME_FORMAT);
    }
}
