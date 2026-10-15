package com.ccnlthd.taskmanager;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.repository.UserRepository;

/** Integration test: full Spring context + H2, real security filter chain, controllers, services and repository. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void clean() {
        users.deleteAll();
    }

    private User save(String username, String email, String rawPassword, User.Role role, boolean enabled) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setRole(role);
        u.setStatus(enabled);
        return users.save(u);
    }

    private void register(String username, String email, String pw, String confirm) throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                .param("username", username).param("email", email)
                .param("password", pw).param("confirm", confirm));
    }

    // ---------- register ----------

    @Test
    void registerCreatesUserWithHashedPasswordAndRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                .param("username", "an").param("email", "an@example.com")
                .param("password", "secret1").param("confirm", "secret1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        User saved = users.findByUsername("an").orElseThrow();
        assertNotEquals("secret1", saved.getPassword());
        assertTrue(encoder.matches("secret1", saved.getPassword()));
        assertTrue(saved.getRole() == User.Role.USER);
    }

    @Test
    void registerRejectsDuplicateUsernameAndInvalidInput() throws Exception {
        save("an", "an@example.com", "secret1", User.Role.USER, true);

        mockMvc.perform(post("/register").with(csrf())
                .param("username", "an").param("email", "other@example.com")
                .param("password", "secret1").param("confirm", "secret1"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Tên đăng nhập đã tồn tại"));

        mockMvc.perform(post("/register").with(csrf())
                .param("username", "b").param("email", "not-an-email")
                .param("password", "secret1").param("confirm", "secret1"))
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"));

        mockMvc.perform(post("/register").with(csrf())
                .param("username", "c").param("email", "c@example.com")
                .param("password", "secret1").param("confirm", "different"))
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Mật khẩu xác nhận không khớp"));

        assertTrue(users.findByUsername("b").isEmpty());
        assertTrue(users.findByUsername("c").isEmpty());
    }

    // ---------- login ----------

    @Test
    void registeredUserCanLoginAndReachDashboardAndTasksButNotAdmin() throws Exception {
        register("an", "an@example.com", "secret1", "secret1");

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/login").with(csrf())
                .param("identity", "an").param("password", "secret1"))
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn().getRequest().getSession(false);

        mockMvc.perform(get("/dashboard").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/tasks").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/admin").session(session)).andExpect(forwardedUrl("/403"));
    }

    @Test
    void loginByEmailWorksAndAdminIsRedirectedToAdminHome() throws Exception {
        save("root", "root@example.com", "adminpw", User.Role.ADMIN, true);

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/login").with(csrf())
                .param("identity", "root@example.com").param("password", "adminpw"))
                .andExpect(redirectedUrl("/admin"))
                .andReturn().getRequest().getSession(false);

        mockMvc.perform(get("/admin").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/tasks").session(session)).andExpect(forwardedUrl("/403"));
    }

    @Test
    void loginFailsWithWrongPasswordUnknownUserOrBlankFields() throws Exception {
        save("an", "an@example.com", "secret1", User.Role.USER, true);

        mockMvc.perform(post("/login").with(csrf()).param("identity", "an").param("password", "wrong"))
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Sai tài khoản hoặc mật khẩu"));
        mockMvc.perform(post("/login").with(csrf()).param("identity", "ghost").param("password", "secret1"))
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Sai tài khoản hoặc mật khẩu"));
        mockMvc.perform(post("/login").with(csrf()).param("identity", "").param("password", ""))
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void lockedAccountCannotLogin() throws Exception {
        save("locked", "locked@example.com", "secret1", User.Role.USER, false);

        mockMvc.perform(post("/login").with(csrf()).param("identity", "locked").param("password", "secret1"))
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Tài khoản đã bị khóa"));
    }

    @Test
    void accountLockedAfterLoginIsKickedOutOnNextRequest() throws Exception {
        User u = save("an", "an@example.com", "secret1", User.Role.USER, true);

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/login").with(csrf())
                .param("identity", "an").param("password", "secret1"))
                .andReturn().getRequest().getSession(false);

        u.setStatus(false);
        users.save(u);

        mockMvc.perform(get("/tasks").session(session)).andExpect(redirectedUrl("/login?locked"));
    }

    // ---------- authorization ----------

    @Test
    void anonymousIsRedirectedToLoginForProtectedPages() throws Exception {
        for (String path : new String[] { "/tasks", "/admin", "/admin/users", "/account" }) {
            mockMvc.perform(get(path)).andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }

    @Test
    void publicPagesAreAccessibleWithoutLogin() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());
        mockMvc.perform(get("/403")).andExpect(status().isOk());
        mockMvc.perform(get("/api/hello")).andExpect(status().isOk());
    }

    @Test
    void roleUserCannotAccessAdminAreaAndAdminCannotAccessTasks() throws Exception {
        mockMvc.perform(get("/admin").with(user("an").roles("USER"))).andExpect(forwardedUrl("/403"));
        mockMvc.perform(get("/admin/users").with(user("an").roles("USER"))).andExpect(forwardedUrl("/403"));
        mockMvc.perform(get("/tasks").with(user("root").roles("ADMIN"))).andExpect(forwardedUrl("/403"));
        mockMvc.perform(get("/admin").with(user("root").roles("ADMIN"))).andExpect(status().isOk());
    }

    @Test
    void logoutRedirectsToLoginAndInvalidatesSession() throws Exception {
        save("an", "an@example.com", "secret1", User.Role.USER, true);
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/login").with(csrf())
                .param("identity", "an").param("password", "secret1"))
                .andReturn().getRequest().getSession(false);

        mockMvc.perform(post("/logout").with(csrf()).session(session))
                .andExpect(redirectedUrl("/login?logout"));

        mockMvc.perform(get("/tasks").session(session)).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void postWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/login").param("identity", "an").param("password", "x"))
                .andExpect(status().isForbidden());
    }
}
