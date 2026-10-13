package com.ccnlthd.taskmanager.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ccnlthd.taskmanager.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdAndCategoryIdOrderByDueDateAscIdAsc(Long userId, Long categoryId);
    Task findByIdAndUserId(Long id, Long userId);
    long countByUserIdAndStatusAndDueDate(Long userId, Task.Status status, LocalDate dueDate);
    void deleteByCategoryIdAndUserId(Long categoryId, Long userId);
    void delete(Task task);

    @Query("""
            select t.user.id as userId, count(t) as taskCount
            from Task t
            where t.user.id in :userIds
            group by t.user.id
            """)
    List<UserTaskCount> countByUserIds(@Param("userIds") Collection<Long> userIds);

    long countByStatus(Task.Status status);

    long countByStatusAndDueDateBefore(Task.Status status, LocalDate date);
}