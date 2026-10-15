package com.ccnlthd.taskmanager.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ccnlthd.taskmanager.dto.TaskCreateDTO;
import com.ccnlthd.taskmanager.model.Category;
import com.ccnlthd.taskmanager.model.Task;
import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.repository.TaskRepository;
import com.ccnlthd.taskmanager.repository.TaskStatisticsRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class TaskService {
    private final TaskRepository tasks;
    private final TaskStatisticsRepository taskStatistics;

    public List<Task> findByUserIdAndCategoryIdOrderByDueDateAscIdAsc(Long userId, Long categoryId) {
        return tasks.findByUserIdAndCategoryIdOrderByDueDateAscIdAsc(userId, categoryId);
    }

    public long countByUserIdAndStatusAndDueDate(Long userId, Task.Status status, java.time.LocalDate dueDate) {
        return taskStatistics.countByUserIdAndStatusAndDueDate(userId, status, dueDate);
    }

    @Transactional
    public TaskStatistics getStatisticsForUser(Long userId) {
        LocalDate today = LocalDate.now();
        return new TaskStatistics(
                taskStatistics.countByUserId(userId),
                taskStatistics.countByUserIdAndStatus(userId, Task.Status.COMPLETED),
                taskStatistics.countByUserIdAndStatus(userId, Task.Status.TODO),
                taskStatistics.countByUserIdAndStatusAndDueDateBefore(userId, Task.Status.TODO, today),
                taskStatistics.countByUserIdAndStatusAndDueDate(userId, Task.Status.TODO, today));
    }

    public Task findByIdAndUserId(Long taskId, Long userId) {
        return tasks.findByIdAndUserId(taskId, userId);
    }

    public void deleteTask(Task task) {
        tasks.delete(task);
    }

    public Boolean updateStatusByIdAndUserId(Long taskId, Long userId) {
        Task task = tasks.findByIdAndUserId(taskId, userId);
        if (task == null) {
            throw new RuntimeException("Task không tồn tại");
        }
        task.setStatus(task.isCompleted() ? Task.Status.TODO : Task.Status.COMPLETED);
        tasks.save(task);
        return true;
    }

    @Transactional
    public void deleteByCategoryIdAndUserId(Long categoryId, Long userId) {
        tasks.deleteByCategoryIdAndUserId(categoryId, userId);
    }

    @Transactional
    public Task create(TaskCreateDTO dto, User user, Category category) {

        Task task = new Task();
        task.setTitle(dto.getTitle().trim());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setUser(user);
        task.setCategory(category);
        task.setStatus(Task.Status.TODO);

        return tasks.save(task);
    }

    @Transactional
    public Task update(TaskCreateDTO dto, User user, Category category) {

        Task task = tasks.findByIdAndUserId(dto.getId(), user.getId());

        if (task == null) {
            throw new RuntimeException(
                    "Task không tồn tại"
            );
        }

        task.setTitle(dto.getTitle().trim());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setCategory( category );

        return tasks.save(task);
    }
}
