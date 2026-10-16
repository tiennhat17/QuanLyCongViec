package com.ccnlthd.taskmanager.dto;

import lombok.Getter;

import java.util.List;

/**
 * DTO phẳng cho dashboard Admin — gom tất cả số liệu thống kê ở một nơi.
 * Trước đây logic thống kê nằm trong record {@code AdminUserStatistics};
 * DTO này tách rời để View chỉ đọc dữ liệu đã được chuẩn hóa.
 */
@Getter
public class AdminDashboardDTO {
    private final long totalUsers;
    private final long activeUsers;
    private final long lockedUsers;
    private final long totalTasks;
    private final long completedTasks;
    private final long pendingTasks;
    private final long overdueTasks;
    private final double completionRate;
    private final long totalCategories;
    /** Số chủ sở hữu duy nhất đang có ít nhất 1 danh mục (cho "Tóm tắt nhanh"). */
    private final long uniqueCategoryOwners;
    private final List<AdminUserDTO> recentUsers;
    private final List<AdminTaskDTO> overdueTasksList;
    private final List<AdminCategoryDTO> topCategories;

    public AdminDashboardDTO(long totalUsers,
                             long activeUsers,
                             long lockedUsers,
                             long totalTasks,
                             long completedTasks,
                             long pendingTasks,
                             long overdueTasks,
                             long totalCategories,
                             long uniqueCategoryOwners,
                             List<AdminUserDTO> recentUsers,
                             List<AdminTaskDTO> overdueTasksList,
                             List<AdminCategoryDTO> topCategories) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.lockedUsers = lockedUsers;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.overdueTasks = overdueTasks;
        this.totalCategories = totalCategories;
        this.completionRate = totalTasks == 0 ? 0 : completedTasks * 100.0 / totalTasks;
        this.uniqueCategoryOwners = uniqueCategoryOwners;
        this.recentUsers = recentUsers == null ? List.of() : List.copyOf(recentUsers);
        this.overdueTasksList = overdueTasksList == null ? List.of() : List.copyOf(overdueTasksList);
        this.topCategories = topCategories == null ? List.of() : List.copyOf(topCategories);
    }
}
