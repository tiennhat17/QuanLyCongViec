package com.ccnlthd.taskmanager.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ccnlthd.taskmanager.dto.AdminCategoryDTO;
import com.ccnlthd.taskmanager.dto.AdminDashboardDTO;
import com.ccnlthd.taskmanager.dto.AdminTaskDTO;
import com.ccnlthd.taskmanager.dto.AdminUserDTO;
import com.ccnlthd.taskmanager.model.Category;
import com.ccnlthd.taskmanager.model.Task;
import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.repository.CategoryRepository;
import com.ccnlthd.taskmanager.repository.TaskRepository;
import com.ccnlthd.taskmanager.repository.UserRepository;
import com.ccnlthd.taskmanager.repository.UserTaskCount;

import lombok.RequiredArgsConstructor;

/**
 * Service chuyên trách cho module Admin: gom toàn bộ truy vấn
 * thống kê/dashboard vào một nơi, tách khỏi {@link UserService}
 * (vốn chỉ nên lo nghiệp vụ tài khoản người dùng).
 *
 * <p>Toàn bộ method ở đây đều {@code readOnly = true} — chỉ đọc dữ liệu,
 * không thực hiện ghi.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository users;
    private final TaskRepository tasks;
    private final CategoryRepository categories;

    /**
     * Gom toàn bộ chỉ số cho dashboard admin:
     * <ul>
     *   <li>User: tổng / active / locked.</li>
     *   <li>Task: tổng / hoàn thành / chờ / trễ hạn / completion rate.</li>
     *   <li>Category: tổng / số chủ sở hữu duy nhất.</li>
     *   <li>5 user mới nhất kèm số task thật (1 query gom nhóm, tránh N+1).</li>
     * </ul>
     */
    public AdminDashboardDTO getAdminDashboard() {
        long totalUsers = users.count();
        long activeUsers = users.countByStatus(true);
        long lockedUsers = users.countByStatus(false);
        long totalTasks = tasks.count();
        long completedTasks = tasks.countByStatus(Task.Status.COMPLETED);
        long pendingTasks = tasks.countByStatus(Task.Status.TODO);
        long overdueTasks = tasks.countByStatusAndDueDateBefore(
                Task.Status.TODO, java.time.LocalDate.now());
        long totalCategories = categories.count();
        long uniqueOwners = categories.countDistinctUserId();

        // 5 user mới nhất + số task thật — 1 query gom nhóm, không N+1
        List<User> latest = users.findTop5ByOrderByCreatedAtDesc();
        List<Long> latestIds = latest.stream().map(User::getId).toList();
        Map<Long, Long> latestCounts = new HashMap<>();
        if (!latestIds.isEmpty()) {
            for (UserTaskCount c : tasks.countByUserIds(latestIds)) {
                latestCounts.put(c.getUserId(), c.getTaskCount());
            }
        }
        List<AdminUserDTO> recentUsers = latest.stream()
                .map(u -> new AdminUserDTO(u, latestCounts.getOrDefault(u.getId(), 0L)))
                .toList();

        return new AdminDashboardDTO(
                totalUsers, activeUsers, lockedUsers,
                totalTasks, completedTasks, pendingTasks, overdueTasks,
                totalCategories, uniqueOwners,
                recentUsers,
                List.of(), // overdueTasksList: bổ sung sau nếu cần
                List.of()  // topCategories: bổ sung sau nếu cần
        );
    }

    /**
     * Record thống kê cũ (giữ lại để tương thích ngược với
     * controller/task cũ nếu có). Phần lớn dữ liệu đã được thay
     * thế bằng {@link AdminDashboardDTO}.
     */
    public AdminUserStatistics getAdminUserStatistics() {
        long totalUsers = users.count();
        long activeUsers = users.countByStatus(true);
        long lockedUsers = users.countByStatus(false);
        long totalTasks = tasks.count();
        long completedTasks = tasks.countByStatus(Task.Status.COMPLETED);
        long pendingTasks = tasks.countByStatus(Task.Status.TODO);
        long overdueTasks = tasks.countByStatusAndDueDateBefore(
                Task.Status.TODO, java.time.LocalDate.now());
        long totalCategories = categories.count();
        List<AdminUserDTO> recentUsers = users.findTop5ByOrderByCreatedAtDesc().stream()
                .map(user -> new AdminUserDTO(user, 0))
                .toList();
        return new AdminUserStatistics(totalUsers, activeUsers, lockedUsers,
                recentUsers, totalTasks, completedTasks,
                pendingTasks, overdueTasks, totalCategories);
    }

    /**
     * Helper: lấy map userId → taskCount từ một danh sách user.
     * Dùng cho mọi nơi cần render danh sách user kèm số task.
     */
    public Map<Long, Long> countTasksByUserIds(List<Long> userIds) {
        Map<Long, Long> counts = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return counts;
        }
        for (UserTaskCount c : tasks.countByUserIds(userIds)) {
            counts.put(c.getUserId(), c.getTaskCount());
        }
        return counts;
    }

    /**
     * Lấy top N task trễ hạn (chưa hoàn thành, dueDate &lt; hôm nay).
     * Dùng cho dashboard / trang thống kê.
     */
    public List<AdminTaskDTO> findOverdueTasks(int limit) {
        return tasks.findByStatusAndDueDateBeforeOrderByDueDateAsc(
                        Task.Status.TODO, java.time.LocalDate.now(),
                        org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(AdminTaskDTO::new)
                .toList();
    }

    /**
     * Top N danh mục có nhiều task nhất — phục vụ dashboard.
     * Trả về DTO phẳng, đã chuẩn hóa ngày tạo.
     */
    public List<AdminCategoryDTO> findTopCategories(int limit) {
        List<Category> top = categories.findTopCategoriesByTaskCount(
                org.springframework.data.domain.PageRequest.of(0, limit));
        return top.stream().map(AdminCategoryDTO::new).toList();
    }
}
