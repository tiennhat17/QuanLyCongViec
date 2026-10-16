package com.ccnlthd.taskmanager.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ccnlthd.taskmanager.dto.AdminUserDTO;
import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service chuyên trách cho các thao tác admin lên người dùng:
 * tra cứu có phân trang, khóa/mở tài khoản.
 *
 * <p>Tách khỏi {@link UserService} (chỉ lo nghiệp vụ tài khoản cá nhân)
 * và {@link AdminDashboardService} (chỉ thống kê) để mỗi service có một
 * trách nhiệm rõ ràng.</p>
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository users;
    private final AdminDashboardService adminDashboardService;

    /**
     * Tìm user theo từ khóa (username hoặc email), phân trang, kèm số task.
     *
     * <p>Số task được lấy bằng 1 query gom nhóm thông qua
     * {@link AdminDashboardService#countTasksByUserIds(List)} — tránh N+1
     * khi map từng {@code User} sang {@code AdminUserDTO}.</p>
     */
    @Transactional(readOnly = true)
    public Page<AdminUserDTO> findUsers(String search, Pageable pageable) {
        String keyword = search == null ? "" : search.trim();
        Page<User> result = users.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                keyword, keyword, pageable);
        List<Long> userIds = result.getContent().stream().map(User::getId).toList();
        Map<Long, Long> counts = adminDashboardService.countTasksByUserIds(userIds);
        return result.map(user -> new AdminUserDTO(user, counts.getOrDefault(user.getId(), 0L)));
    }

    /**
     * Khóa / mở khóa tài khoản. Admin không được tự khóa chính mình.
     */
    @Transactional
    public void updateStatus(Long targetId, String adminUsername, boolean enabled) {
        User target = users.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        if (!enabled && target.getUsername().equals(adminUsername)) {
            throw new AccessDeniedException("Admin không thể tự khóa tài khoản của mình");
        }
        target.setStatus(enabled);
    }
}
