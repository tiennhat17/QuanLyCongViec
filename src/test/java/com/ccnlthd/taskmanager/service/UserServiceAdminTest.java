package com.ccnlthd.taskmanager.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceAdminTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService service;

    @Test
    void findUsersTrimsSearchAndDelegatesPageable() {
        Pageable pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(users.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                "alice", "alice", pageable)).thenReturn(new PageImpl<>(java.util.List.of()));

        service.findUsers("  alice  ", pageable);

        verify(users).findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                "alice", "alice", pageable);
    }

    @Test
    void updateStatusLocksAnotherUser() {
        User target = user(2L, "alice", true);
        when(users.findById(2L)).thenReturn(Optional.of(target));

        service.updateStatus(2L, "admin", false);

        assertFalse(target.isStatus());
    }

    @Test
    void updateStatusUnlocksAnotherUser() {
        User target = user(2L, "alice", false);
        when(users.findById(2L)).thenReturn(Optional.of(target));

        service.updateStatus(2L, "admin", true);

        assertTrue(target.isStatus());
    }

    @Test
    void updateStatusRejectsSelfLock() {
        User admin = user(1L, "admin", true);
        when(users.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class,
                () -> service.updateStatus(1L, "admin", false));
        assertTrue(admin.isStatus());
    }

    @Test
    void updateStatusRejectsUnknownUser() {
        when(users.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.updateStatus(99L, "admin", false));
    }

    @Test
    void getAdminUserStatisticsCountsStatusesAndReturnsRecentUsers() {
        User recent = user(3L, "recent", true);
        when(users.count()).thenReturn(12L);
        when(users.countByStatus(true)).thenReturn(9L);
        when(users.countByStatus(false)).thenReturn(3L);
        when(users.findTop5ByOrderByCreatedAtDesc()).thenReturn(java.util.List.of(recent));

        AdminUserStatistics result = service.getAdminUserStatistics();

        assertEquals(12L, result.totalUsers());
        assertEquals(9L, result.activeUsers());
        assertEquals(3L, result.lockedUsers());
        assertEquals(java.util.List.of(recent), result.recentUsers());
        verify(users).count();
        verify(users).countByStatus(true);
        verify(users).countByStatus(false);
        verify(users).findTop5ByOrderByCreatedAtDesc();
    }

    private User user(Long id, String username, boolean status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setStatus(status);
        return user;
    }
}
