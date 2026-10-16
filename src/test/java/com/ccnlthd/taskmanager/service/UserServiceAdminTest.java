package com.ccnlthd.taskmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.repository.UserRepository;

/**
 * Test cho {@link AdminUserService} sau khi tách khỏi {@code UserService}.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceAdminTest {

    @Mock
    private UserRepository users;

    @Mock
    private AdminDashboardService adminDashboardService;

    private AdminUserService newService() {
        return new AdminUserService(users, adminDashboardService);
    }

    @Test
    void updateStatusLocksAnotherUser() {
        User target = user(2L, "alice", true);
        when(users.findById(2L)).thenReturn(Optional.of(target));

        newService().updateStatus(2L, "admin", false);

        assertFalse(target.isStatus());
    }

    @Test
    void updateStatusUnlocksAnotherUser() {
        User target = user(2L, "alice", false);
        when(users.findById(2L)).thenReturn(Optional.of(target));

        newService().updateStatus(2L, "admin", true);

        assertTrue(target.isStatus());
    }

    @Test
    void updateStatusRejectsSelfLock() {
        User admin = user(1L, "admin", true);
        when(users.findById(1L)).thenReturn(Optional.of(admin));

        AdminUserService service = newService();
        assertThrows(AccessDeniedException.class,
                () -> service.updateStatus(1L, "admin", false));
        assertTrue(admin.isStatus());
    }

    @Test
    void updateStatusRejectsUnknownUser() {
        when(users.findById(99L)).thenReturn(Optional.empty());

        AdminUserService service = newService();
        assertThrows(IllegalArgumentException.class,
                () -> service.updateStatus(99L, "admin", false));
    }

    private User user(Long id, String username, boolean status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setStatus(status);
        return user;
    }
}
