package com.ccnlthd.taskmanager.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ccnlthd.taskmanager.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdAndCategoryIdOrderByDueDateAscIdAsc(Long userId, Long categoryId);
    Task findByIdAndUserId(Long id, Long userId);
    long countByUserIdAndStatusAndDueDate(Long userId, Task.Status status, LocalDate dueDate);
    void deleteByCategoryIdAndUserId(Long categoryId, Long userId);
    void delete(Task task);

}