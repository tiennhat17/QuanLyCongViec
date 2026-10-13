package com.ccnlthd.taskmanager.service;

import java.util.List;

import com.ccnlthd.taskmanager.dto.AdminUserDTO;

public record AdminUserStatistics(
        long totalUsers,
        long activeUsers,
        long lockedUsers,
        List<AdminUserDTO> recentUsers,
        long totalTasks,
        long completedTasks,
        long pendingTasks,
        long overdueTasks,
        long totalCategories) {

    public AdminUserStatistics(long totalUsers, long activeUsers, long lockedUsers,
                               List<AdminUserDTO> recentUsers) {
        this(totalUsers, activeUsers, lockedUsers, recentUsers, 0, 0, 0, 0, 0);
    }

    public double completionRate() {
        return totalTasks == 0 ? 0 : completedTasks * 100.0 / totalTasks;
    }

    public long getTotalUsers() { return totalUsers; }
    public long getActiveUsers() { return activeUsers; }
    public long getLockedUsers() { return lockedUsers; }
    public List<AdminUserDTO> getRecentUsers() { return recentUsers; }
    public long getTotalTasks() { return totalTasks; }
    public long getCompletedTasks() { return completedTasks; }
    public long getPendingTasks() { return pendingTasks; }
    public long getOverdueTasks() { return overdueTasks; }
    public long getTotalCategories() { return totalCategories; }
    public double getCompletionRate() { return completionRate(); }
}
