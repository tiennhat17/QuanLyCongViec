package com.ccnlthd.taskmanager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.model.User.Role;
import com.ccnlthd.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Transactional
    public User register(String username, String email, String password) {
        return register(username, email, password, 0);
    }

    @Transactional
    public User register(String username, String email, String password, int clientTimezoneOffsetMinutes) {
        if (users.findByUsername(username).isPresent()) throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        if (users.findByEmail(email).isPresent()) throw new IllegalArgumentException("Email đã được sử dụng");
        if (clientTimezoneOffsetMinutes < -840 || clientTimezoneOffsetMinutes > 840) {
            throw new IllegalArgumentException("Múi giờ không hợp lệ");
        }
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setRole(Role.USER);
        u.setCreatedAt(LocalDateTime.now(ZoneOffset.ofTotalSeconds(-clientTimezoneOffsetMinutes * 60)));
        users.save(u);
        /*Category inbox = new Category();   // danh mục mặc định cho người dùng mới
        inbox.setName("Hộp thư đến");
        inbox.setUser(u);
        categories.save(inbox); */
        return u;
    }

    
    public User login(String identity, String password) {
        User u = users.findByUsernameOrEmail(identity, identity)
                .filter(x -> encoder.matches(password, x.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("Sai tài khoản hoặc mật khẩu"));
        if (!u.isStatus()) throw new IllegalArgumentException("Tài khoản đã bị khóa");
        return u;
    }

    @Transactional(readOnly = true)
    public Page<User> findUsers(String search, Pageable pageable) {
        String keyword = search == null ? "" : search.trim();
        return users.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                keyword, keyword, pageable);
    }

    @Transactional(readOnly = true)
    public AdminUserStatistics getAdminUserStatistics() {
        long totalUsers = users.count();
        long activeUsers = users.countByStatus(true);
        long lockedUsers = users.countByStatus(false);
        return new AdminUserStatistics(totalUsers, activeUsers, lockedUsers,
                users.findTop5ByOrderByCreatedAtDesc());
    }

    @Transactional
    public void updateStatus(Long targetId, String adminUsername, boolean enabled) {
        User target = users.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        if (!enabled && target.getUsername().equals(adminUsername)) {
            throw new IllegalArgumentException("Admin không thể tự khóa tài khoản của mình");
        }
        target.setStatus(enabled);
    }

    @Transactional(readOnly = true)
    public User getAccount(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
    }

    @Transactional
    public User updateProfile(String currentUsername, String username, String email) {
        User account = getAccount(currentUsername);
        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedEmail = email == null ? "" : email.trim();
        if (normalizedUsername.isBlank() || normalizedUsername.length() > 50) {
            throw new IllegalArgumentException("Tên đăng nhập phải từ 1 đến 50 ký tự");
        }
        if (normalizedEmail.length() > 100 || !normalizedEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
        if (users.findByUsername(normalizedUsername)
                .filter(existing -> !existing.getId().equals(account.getId())).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (users.findByEmail(normalizedEmail)
                .filter(existing -> !existing.getId().equals(account.getId())).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }
        account.setUsername(normalizedUsername);
        account.setEmail(normalizedEmail);
        return account;
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword,
                               String confirmPassword) {
        User account = getAccount(username);
        if (currentPassword == null || !encoder.matches(currentPassword, account.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới tối thiểu 6 ký tự");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }
        if (encoder.matches(newPassword, account.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        account.setPassword(encoder.encode(newPassword));
    }
}
