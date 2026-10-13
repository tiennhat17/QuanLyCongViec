package com.ccnlthd.taskmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceAuthTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService service;

    private User existing(boolean enabled) {
        User u = new User();
        u.setId(1L);
        u.setUsername("an");
        u.setEmail("an@example.com");
        u.setPassword("HASH");
        u.setRole(User.Role.USER);
        u.setStatus(enabled);
        return u;
    }

    // ---------- register ----------

    @Test
    void registerSavesUserWithEncodedPasswordAndUserRole() {
        when(users.findByUsername("an")).thenReturn(Optional.empty());
        when(users.findByEmail("an@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("secret1")).thenReturn("HASH");

        User result = service.register("an", "an@example.com", "secret1");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertSame(result, saved.getValue());
        assertEquals("an", result.getUsername());
        assertEquals("an@example.com", result.getEmail());
        assertEquals("HASH", result.getPassword());
        assertNotEquals("secret1", result.getPassword());
        assertEquals(User.Role.USER, result.getRole());
        assertTrue(result.isStatus());
        assertTrue(result.getCreatedAt() != null);
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(users.findByUsername("an")).thenReturn(Optional.of(existing(true)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.register("an", "new@example.com", "secret1"));

        assertEquals("Tên đăng nhập đã tồn tại", ex.getMessage());
        verify(users, never()).save(any());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(users.findByUsername("new")).thenReturn(Optional.empty());
        when(users.findByEmail("an@example.com")).thenReturn(Optional.of(existing(true)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.register("new", "an@example.com", "secret1"));

        assertEquals("Email đã được sử dụng", ex.getMessage());
        verify(users, never()).save(any());
    }

    @Test
    void registerRejectsTimezoneOutOfRange() {
        when(users.findByUsername("an")).thenReturn(Optional.empty());
        when(users.findByEmail("an@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.register("an", "an@example.com", "secret1", 841));
        assertThrows(IllegalArgumentException.class,
                () -> service.register("an", "an@example.com", "secret1", -841));
        verify(users, never()).save(any());
    }

    @Test
    void registerAcceptsTimezoneBoundaries() {
        when(users.findByUsername("an")).thenReturn(Optional.empty());
        when(users.findByEmail("an@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("secret1")).thenReturn("HASH");

        assertEquals("an", service.register("an", "an@example.com", "secret1", 840).getUsername());
        assertEquals("an", service.register("an", "an@example.com", "secret1", -840).getUsername());
    }

    // ---------- login ----------

    @Test
    void loginSucceedsWithUsernameOrEmail() {
        User u = existing(true);
        when(users.findByUsernameOrEmail("an", "an")).thenReturn(Optional.of(u));
        when(users.findByUsernameOrEmail("an@example.com", "an@example.com")).thenReturn(Optional.of(u));
        when(encoder.matches("secret1", "HASH")).thenReturn(true);

        assertSame(u, service.login("an", "secret1"));
        assertSame(u, service.login("an@example.com", "secret1"));
    }

    @Test
    void loginFailsWithWrongPassword() {
        when(users.findByUsernameOrEmail("an", "an")).thenReturn(Optional.of(existing(true)));
        when(encoder.matches("wrong", "HASH")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.login("an", "wrong"));

        assertEquals("Sai tài khoản hoặc mật khẩu", ex.getMessage());
    }

    @Test
    void loginFailsWhenUserDoesNotExist() {
        when(users.findByUsernameOrEmail("ghost", "ghost")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.login("ghost", "secret1"));

        assertEquals("Sai tài khoản hoặc mật khẩu", ex.getMessage());
        verify(encoder, never()).matches(any(), any());
    }

    @Test
    void loginFailsWhenAccountIsLocked() {
        when(users.findByUsernameOrEmail("an", "an")).thenReturn(Optional.of(existing(false)));
        when(encoder.matches("secret1", "HASH")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.login("an", "secret1"));

        assertEquals("Tài khoản đã bị khóa", ex.getMessage());
    }
}
