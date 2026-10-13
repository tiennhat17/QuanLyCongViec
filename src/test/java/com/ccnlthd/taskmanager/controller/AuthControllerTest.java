package com.ccnlthd.taskmanager.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.ccnlthd.taskmanager.config.SecurityConfig;
import com.ccnlthd.taskmanager.dto.request.LoginRequest;
import com.ccnlthd.taskmanager.dto.request.RegisterRequest;
import com.ccnlthd.taskmanager.service.AuthService;
import com.ccnlthd.taskmanager.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    private final MockHttpServletRequest req = new MockHttpServletRequest();
    private final MockHttpServletResponse res = new MockHttpServletResponse();

    private Authentication auth(String role) {
        return UsernamePasswordAuthenticationToken.authenticated("u", null,
                List.of(new SimpleGrantedAuthority(role)));
    }

    private LoginRequest login(String id, String pw) {
        LoginRequest r = new LoginRequest();
        r.setIdentity(id);
        r.setPassword(pw);
        return r;
    }

    private RegisterRequest register() {
        RegisterRequest r = new RegisterRequest();
        r.setUsername("an");
        r.setEmail("an@example.com");
        r.setPassword("secret1");
        r.setConfirm("secret1");
        return r;
    }

    private BindingResult noErrors(Object target) {
        return new BeanPropertyBindingResult(target, "target");
    }

    private BindingResult withError(Object target, String message) {
        BindingResult br = new BeanPropertyBindingResult(target, "target");
        br.reject("invalid", message);
        return br;
    }

    // ---------- homeFor ----------

    @Test
    void homeForReturnsAdminOrTasksByRole() {
        assertEquals("/admin", SecurityConfig.homeFor(auth("ROLE_ADMIN")));
        assertEquals("/tasks", SecurityConfig.homeFor(auth("ROLE_USER")));
    }

    // ---------- GET /login ----------

    @Test
    void loginFormShowsLoginPageForAnonymous() {
        Authentication anon = new AnonymousAuthenticationToken("k", "anon",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        assertEquals("login", controller.loginForm(null, null, null, anon, new ConcurrentModel()));
        assertEquals("login", controller.loginForm(null, null, null, null, new ConcurrentModel()));
    }

    @Test
    void loginFormRedirectsAuthenticatedUsersToTheirHome() {
        assertEquals("redirect:/admin",
                controller.loginForm(null, null, null, auth("ROLE_ADMIN"), new ConcurrentModel()));
        assertEquals("redirect:/tasks",
                controller.loginForm(null, null, null, auth("ROLE_USER"), new ConcurrentModel()));
    }

    @Test
    void loginFormAddsMessagesForErrorLockedAndLogout() {
        ConcurrentModel m1 = new ConcurrentModel();
        controller.loginForm("1", null, null, null, m1);
        assertEquals("Sai tên đăng nhập hoặc mật khẩu", m1.getAttribute("error"));

        ConcurrentModel m2 = new ConcurrentModel();
        controller.loginForm(null, null, "1", null, m2);
        assertEquals("Tài khoản đã bị khóa", m2.getAttribute("error"));

        ConcurrentModel m3 = new ConcurrentModel();
        controller.loginForm(null, "1", null, null, m3);
        assertEquals("Đã đăng xuất", m3.getAttribute("msg"));
    }

    // ---------- POST /login ----------

    @Test
    void loginReturnsFormWithFirstValidationError() {
        LoginRequest r = login("", "");
        ConcurrentModel m = new ConcurrentModel();

        assertEquals("login", controller.login(r, withError(r, "Vui lòng nhập mật khẩu"), req, res, m));

        assertEquals("Vui lòng nhập mật khẩu", m.getAttribute("error"));
        verify(authService, never()).login(any(), any(), any());
    }

    @Test
    void loginRedirectsUserAndAdminToTheirHome() {
        LoginRequest r = login("an", "pw");
        when(authService.login(r, req, res)).thenReturn(auth("ROLE_USER"));
        assertEquals("redirect:/tasks", controller.login(r, noErrors(r), req, res, new ConcurrentModel()));

        LoginRequest a = login("root", "pw");
        when(authService.login(a, req, res)).thenReturn(auth("ROLE_ADMIN"));
        assertEquals("redirect:/admin", controller.login(a, noErrors(a), req, res, new ConcurrentModel()));
    }

    @Test
    void loginShowsServiceErrorAndKeepsIdentity() {
        LoginRequest r = login("an", "bad");
        when(authService.login(r, req, res)).thenThrow(new IllegalArgumentException("Sai tài khoản hoặc mật khẩu"));
        ConcurrentModel m = new ConcurrentModel();

        assertEquals("login", controller.login(r, noErrors(r), req, res, m));

        assertEquals("Sai tài khoản hoặc mật khẩu", m.getAttribute("error"));
        assertEquals("an", m.getAttribute("identity"));
    }

    // ---------- POST /register ----------

    @Test
    void registerReturnsFormOnValidationError() {
        RegisterRequest r = register();
        ConcurrentModel m = new ConcurrentModel();

        assertEquals("register", controller.register(r, withError(r, "Email không hợp lệ"), m,
                new RedirectAttributesModelMap()));

        assertEquals("Email không hợp lệ", m.getAttribute("error"));
        verify(userService, never()).register(any(), any(), any(), anyInt());
    }

    @Test
    void registerRedirectsToLoginWithFlashMessageOnSuccess() {
        RegisterRequest r = register();
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        assertEquals("redirect:/login", controller.register(r, noErrors(r), new ConcurrentModel(), ra));

        verify(userService).register("an", "an@example.com", "secret1", 0);
        assertEquals("Đăng ký thành công, hãy đăng nhập.", ra.getFlashAttributes().get("msg"));
    }

    @Test
    void registerShowsServiceErrorOnDuplicate() {
        RegisterRequest r = register();
        when(userService.register("an", "an@example.com", "secret1", 0))
                .thenThrow(new IllegalArgumentException("Tên đăng nhập đã tồn tại"));
        ConcurrentModel m = new ConcurrentModel();
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        assertEquals("register", controller.register(r, noErrors(r), m, ra));

        assertEquals("Tên đăng nhập đã tồn tại", m.getAttribute("error"));
        assertEquals("an", m.getAttribute("username"));
        assertNull(ra.getFlashAttributes().get("msg"));
    }
}
