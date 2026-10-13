package com.ccnlthd.taskmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import com.ccnlthd.taskmanager.dto.request.LoginRequest;
import com.ccnlthd.taskmanager.model.User;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private LoginRequest request(String identity, String password) {
        LoginRequest r = new LoginRequest();
        r.setIdentity(identity);
        r.setPassword(password);
        return r;
    }

    private User user(String name, User.Role role) {
        User u = new User();
        u.setUsername(name);
        u.setRole(role);
        return u;
    }

    @Test
    void loginGrantsRoleUserAndStoresContextInSession() {
        when(userService.login("an", "pw")).thenReturn(user("an", User.Role.USER));
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        Authentication auth = authService.login(request("an", "pw"), req, res);

        assertEquals("an", auth.getName());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertEquals(1, auth.getAuthorities().size());
        assertEquals(auth, SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(req.getSession(false).getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
    }

    @Test
    void loginGrantsRoleAdminForAdminUser() {
        when(userService.login("root", "pw")).thenReturn(user("root", User.Role.ADMIN));

        Authentication auth = authService.login(request("root", "pw"),
                new MockHttpServletRequest(), new MockHttpServletResponse());

        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loginChangesSessionIdWhenSessionAlreadyExists() {
        when(userService.login("an", "pw")).thenReturn(user("an", User.Role.USER));
        MockHttpServletRequest req = new MockHttpServletRequest();
        String before = req.getSession(true).getId();

        authService.login(request("an", "pw"), req, new MockHttpServletResponse());

        assertNotEquals(before, req.getSession(false).getId());
    }

    @Test
    void loginPropagatesErrorAndDoesNotAuthenticate() {
        when(userService.login("an", "bad")).thenThrow(new IllegalArgumentException("Sai tài khoản hoặc mật khẩu"));
        MockHttpServletRequest req = new MockHttpServletRequest();

        assertThrows(IllegalArgumentException.class,
                () -> authService.login(request("an", "bad"), req, new MockHttpServletResponse()));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(req.getSession(false));
    }
}
