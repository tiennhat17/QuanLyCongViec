package com.ccnlthd.taskmanager.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.model.User.Role;
import com.ccnlthd.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service nghiệp vụ tài khoản cá nhân: đăng ký, đăng nhập, tra cứu,
 * cập nhật hồ sơ, đổi mật khẩu.
 *
 * <p>Các thao tác dành riêng cho admin (thống kê dashboard, khóa/mở tài khoản,
 * tìm kiếm user có phân trang) đã được tách sang
 * {@link AdminDashboardService} và {@link AdminUserService}.</p>
 */
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
        return u;
    }

    public User login(String identity, String password) {
        User u = users.findByUsernameOrEmail(identity, identity)
                .filter(x -> encoder.matches(password, x.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("Sai tài khoản hoặc mật khẩu"));
        if (!u.isStatus()) throw new IllegalArgumentException("Tài khoản đã bị khóa");
        return u;
    }

    public User findByUsername(String username) {
        return users.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));
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
