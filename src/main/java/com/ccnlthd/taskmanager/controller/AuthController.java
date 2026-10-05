package com.ccnlthd.taskmanager.controller;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ccnlthd.taskmanager.config.SecurityConfig;
import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller 
@RequiredArgsConstructor 
public class AuthController {
    private final UserService service;
    private final SecurityContextRepository contextRepo = new HttpSessionSecurityContextRepository();
    private static final String EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    @GetMapping("/")
    public String root() { return "redirect:/login"; }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Authentication auth, Model m) {
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken))
            return "redirect:" + SecurityConfig.homeFor(auth);
        if (error != null) m.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu");
        if (logout != null) m.addAttribute("msg", "Đã đăng xuất");
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String identity, @RequestParam String password,
                        HttpServletRequest req, HttpServletResponse res, Model m) {
        try {
            User u = service.login(identity.trim(), password);
            Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
                    u.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
            if (req.getSession(false) != null) req.changeSessionId(); // chống session fixation
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(auth);
            SecurityContextHolder.setContext(ctx);
            contextRepo.saveContext(ctx, req, res);
            return "redirect:" + SecurityConfig.homeFor(auth);
        } catch (IllegalArgumentException e) {
            m.addAttribute("error", e.getMessage());
            m.addAttribute("identity", identity);
            return "login";
        }
    }

    @GetMapping("/403")
    public String forbidden() { return "403"; }

    @GetMapping("/tasks")
    public String tasks() { return "tasks"; }

    @GetMapping("/admin")
    public String admin() { return "admin"; }

    @GetMapping("/register")
    public String registerForm() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String email, @RequestParam String password,
                           @RequestParam String confirm, Model m, RedirectAttributes ra) {
        try {
            if (username.isBlank() || username.length() > 50) throw new IllegalArgumentException("Tên đăng nhập 1–50 ký tự");
            if (!email.trim().matches(EMAIL)) throw new IllegalArgumentException("Email không hợp lệ");
            if (password.length() < 6) throw new IllegalArgumentException("Mật khẩu tối thiểu 6 ký tự");
            if (!password.equals(confirm)) throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
            service.register(username.trim(), email.trim(), password);
            ra.addFlashAttribute("msg", "Đăng ký thành công, hãy đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            m.addAttribute("error", e.getMessage());
            m.addAttribute("username", username);
            m.addAttribute("email", email);
            return "register";
        }
    }
}
