package com.ccnlthd.taskmanager.service;

import java.util.Locale.Category;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (users.findByUsername(username).isPresent()) throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        if (users.findByEmail(email).isPresent()) throw new IllegalArgumentException("Email đã được sử dụng");
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setRole(Role.USER);
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
}
