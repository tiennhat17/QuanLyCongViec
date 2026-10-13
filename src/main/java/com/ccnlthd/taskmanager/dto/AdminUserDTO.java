package com.ccnlthd.taskmanager.dto;

import java.time.format.DateTimeFormatter;

import com.ccnlthd.taskmanager.model.User;

import lombok.Getter;

@Getter
public class AdminUserDTO {
    private static final DateTimeFormatter CREATED_AT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Long id;
    private final String username;
    private final String email;
    private final String role;
    private final boolean status;
    private final String formattedCreatedAt;
    private final long taskCount;

    public AdminUserDTO(User user, long taskCount) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole() == null ? "" : user.getRole().name();
        this.status = user.isStatus();
        this.formattedCreatedAt = user.getCreatedAt() == null
                ? ""
                : user.getCreatedAt().format(CREATED_AT_FORMAT);
        this.taskCount = taskCount;
    }
}
