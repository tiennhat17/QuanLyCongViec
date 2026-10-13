package com.ccnlthd.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
    public enum Role { USER, ADMIN }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean status = true;

    private LocalDateTime createdAt;

    public String getFormattedCreatedAt() {
        return createdAt == null ? "" : createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getInitials() {
        String[] p = username.trim().split("\\s+");
        String s = p.length > 1 ? "" + p[0].charAt(0) + p[p.length - 1].charAt(0) : username.substring(0, Math.min(2, username.length()));
        return s.toUpperCase();
    }
}
