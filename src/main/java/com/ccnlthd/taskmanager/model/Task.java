package com.ccnlthd.taskmanager.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Getter @Setter @NoArgsConstructor
public class Task {
    public enum Status { TODO, COMPLETED }

    @Id 
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING) 
    private Status status = Status.TODO;

    private LocalDate dueDate;

    @ManyToOne 
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne 
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;


    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean isCompleted() { 
        return status == Status.COMPLETED; 
    }

    public boolean isOverdue() { 
        return !isCompleted() && dueDate != null && dueDate.isBefore(LocalDate.now()); 
    }

    public String getDueLabel() {
        if (dueDate == null) return "Chưa đặt hạn";
        LocalDate today = LocalDate.now();
        if (dueDate.equals(today)) return "Hôm nay";
        if (dueDate.equals(today.plusDays(1))) return "Ngày mai";
        return dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }


}
