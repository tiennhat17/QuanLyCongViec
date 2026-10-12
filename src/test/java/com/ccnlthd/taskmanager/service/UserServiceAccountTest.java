package com.ccnlthd.taskmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceAccountTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService service;

    @Test
    void updateProfileTrimsFieldsAndPreservesRole() {
        User account = user();
        when(users.findByUsername("alice")).thenReturn(Optional.of(account));
        when(users.findByUsername("alice.nguyen")).thenReturn(Optional.empty());
        when(users.findByEmail("alice.nguyen@example.com")).thenReturn(Optional.empty());

        User updated = service.updateProfile("alice", " alice.nguyen ", " alice.nguyen@example.com ");

        assertEquals("alice.nguyen", updated.getUsername());
        assertEquals("alice.nguyen@example.com", updated.getEmail());
        assertEquals(User.Role.USER, updated.getRole());
    }

    @Test
    void changePasswordEncodesNewPasswordAfterCheckingCurrentPassword() {
        User account = user();
        when(users.findByUsername("alice")).thenReturn(Optional.of(account));
        when(encoder.matches("old-password", "encoded-old")).thenReturn(true);
        when(encoder.matches("new-password", "encoded-old")).thenReturn(false);
        when(encoder.encode("new-password")).thenReturn("encoded-new");

        service.changePassword("alice", "old-password", "new-password", "new-password");

        assertEquals("encoded-new", account.getPassword());
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPassword() {
        User account = user();
        when(users.findByUsername("alice")).thenReturn(Optional.of(account));
        when(encoder.matches("wrong-password", "encoded-old")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.changePassword("alice", "wrong-password", "new-password", "new-password"));
        assertEquals("encoded-old", account.getPassword());
    }

    private User user() {
        User account = new User();
        account.setId(1L);
        account.setUsername("alice");
        account.setEmail("alice@example.com");
        account.setPassword("encoded-old");
        account.setRole(User.Role.USER);
        return account;
    }
}
