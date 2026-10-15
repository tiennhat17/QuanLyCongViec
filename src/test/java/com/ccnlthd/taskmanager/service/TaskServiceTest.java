package com.ccnlthd.taskmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ccnlthd.taskmanager.model.Task;
import com.ccnlthd.taskmanager.repository.TaskRepository;
import com.ccnlthd.taskmanager.repository.TaskStatisticsRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository tasks;

    @Mock
    private TaskStatisticsRepository taskStatistics;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getStatisticsForUserCountsStatusesAndDueDatesForToday() {
        Long userId = 7L;
        LocalDate today = LocalDate.now();
        when(taskStatistics.countByUserId(userId)).thenReturn(5L);
        when(taskStatistics.countByUserIdAndStatus(userId, Task.Status.COMPLETED)).thenReturn(2L);
        when(taskStatistics.countByUserIdAndStatus(userId, Task.Status.TODO)).thenReturn(3L);
        when(taskStatistics.countByUserIdAndStatusAndDueDateBefore(userId, Task.Status.TODO, today)).thenReturn(1L);
        when(taskStatistics.countByUserIdAndStatusAndDueDate(userId, Task.Status.TODO, today)).thenReturn(1L);

        TaskStatistics statistics = taskService.getStatisticsForUser(userId);

        assertEquals(5L, statistics.getTotalTasks());
        assertEquals(2L, statistics.getCompletedTasks());
        assertEquals(3L, statistics.getPendingTasks());
        assertEquals(1L, statistics.getOverdueTasks());
        assertEquals(1L, statistics.getDueTodayTasks());
        verify(taskStatistics).countByUserId(userId);
        verify(taskStatistics).countByUserIdAndStatus(userId, Task.Status.COMPLETED);
        verify(taskStatistics).countByUserIdAndStatus(userId, Task.Status.TODO);
        verify(taskStatistics).countByUserIdAndStatusAndDueDateBefore(userId, Task.Status.TODO, today);
        verify(taskStatistics).countByUserIdAndStatusAndDueDate(userId, Task.Status.TODO, today);
    }
}
