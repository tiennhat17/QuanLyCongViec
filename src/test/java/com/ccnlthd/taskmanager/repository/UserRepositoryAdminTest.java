package com.ccnlthd.taskmanager.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.ccnlthd.taskmanager.model.User;

@SpringBootTest
@ActiveProfiles("test")
class UserRepositoryAdminTest {

    @Autowired
    private UserRepository users;

    @BeforeEach
    void cleanUsers() {
        users.deleteAll();
    }

    @Test
    void searchMatchesUsernameOrEmailCaseInsensitively() {
        users.saveAll(List.of(
                user("Alice", "alice@example.com"),
                user("Bob", "contact@example.com"),
                user("Carol", "carol@example.com")));

        Page<User> result = users.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                "EXAMPLE", "EXAMPLE", PageRequest.of(0, 10));

        assertEquals(3, result.getTotalElements());
    }

    @Test
    void queryPaginatesAndSortsByNewestCreatedAtFirst() {
        for (int i = 1; i <= 11; i++) {
            users.save(user("user" + i, "user" + i + "@example.com"));
        }

        Page<User> firstPage = users.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                "", "", PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        Page<User> secondPage = users.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                "", "", PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertEquals(11, firstPage.getTotalElements());
        assertEquals(10, firstPage.getNumberOfElements());
        assertEquals(1, secondPage.getNumberOfElements());
        assertFalse(firstPage.getContent().get(0).getId()
                .equals(firstPage.getContent().get(9).getId()));
    }

    @Test
    void searchResultsWithMoreThanTenMatchesArePaginated() {
        for (int i = 1; i <= 11; i++) {
            users.save(user("nguyen" + i, "nguyen" + i + "@example.com"));
        }
        users.save(user("other", "other@example.com"));

        Page<User> firstPage = users.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                "nguyen", "nguyen", PageRequest.of(0, 10));
        Page<User> secondPage = users.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                "nguyen", "nguyen", PageRequest.of(1, 10));

        assertEquals(11, firstPage.getTotalElements());
        assertEquals(10, firstPage.getNumberOfElements());
        assertEquals(1, secondPage.getNumberOfElements());
        assertEquals(2, firstPage.getTotalPages());
    }

    @Test
    void statisticsQueriesCountActiveLockedAndLimitRecentUsers() {
        users.saveAll(List.of(
                user("active", "active@example.com"),
                user("locked", "locked@example.com")));
        User locked = users.findByUsername("locked").orElseThrow();
        locked.setStatus(false);
        users.save(locked);

        for (int i = 1; i <= 6; i++) {
            users.save(user("recent" + i, "recent" + i + "@example.com"));
        }

        assertEquals(8, users.count());
        assertEquals(7, users.countByStatus(true));
        assertEquals(1, users.countByStatus(false));
        assertEquals(5, users.findTop5ByOrderByCreatedAtDesc().size());
    }

    private User user(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encoded");
        user.setRole(User.Role.USER);
        return user;
    }
}
