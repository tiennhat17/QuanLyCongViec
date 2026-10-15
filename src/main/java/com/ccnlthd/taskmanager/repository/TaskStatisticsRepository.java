package com.ccnlthd.taskmanager.repository;

import java.time.LocalDate;

import org.springframework.data.repository.Repository;

import com.ccnlthd.taskmanager.model.Task;

public interface TaskStatisticsRepository extends Repository<Task, Long> {
    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, Task.Status status);

    long countByUserIdAndStatusAndDueDate(Long userId, Task.Status status, LocalDate dueDate);

    long countByUserIdAndStatusAndDueDateBefore(Long userId, Task.Status status, LocalDate dueDate);
}
