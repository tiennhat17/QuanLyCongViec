package com.ccnlthd.taskmanager.service;

public class TaskStatistics {
    private final long totalTasks;
    private final long completedTasks;
    private final long pendingTasks;
    private final long overdueTasks;
    private final long dueTodayTasks;

    public TaskStatistics(long totalTasks, long completedTasks, long pendingTasks,
                          long overdueTasks, long dueTodayTasks) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.overdueTasks = overdueTasks;
        this.dueTodayTasks = dueTodayTasks;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public long getPendingTasks() {
        return pendingTasks;
    }

    public long getOverdueTasks() {
        return overdueTasks;
    }

    public long getDueTodayTasks() {
        return dueTodayTasks;
    }
}
