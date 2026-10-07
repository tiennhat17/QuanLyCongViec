package com.ccnlthd.taskmanager.service;

import java.util.List;

import com.ccnlthd.taskmanager.model.User;

public record AdminUserStatistics(
        long totalUsers,
        long activeUsers,
        long lockedUsers,
        List<User> recentUsers) {
}
