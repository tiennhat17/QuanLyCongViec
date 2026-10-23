package com.ccnlthd.taskmanager.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ccnlthd.taskmanager.config.SecurityConfig;
import com.ccnlthd.taskmanager.dto.request.LoginRequest;
import com.ccnlthd.taskmanager.dto.request.RegisterRequest;
import com.ccnlthd.taskmanager.exception.ProblemSupport;
import com.ccnlthd.taskmanager.service.AuthService;
import com.ccnlthd.taskmanager.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller 
@RequiredArgsConstructor 
@Slf4j
public class AuthController {
    private final UserService service;
    private final AuthService authService;

    @GetMapping("/")
    public String root() { return "redirect:/login"; }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String locked,
                            Authentication auth, Model m) {
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken))
            return "redirect:" + SecurityConfig.homeFor(auth);
        if (error != null) m.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu");
        if (locked != null) m.addAttribute("error", "Tài khoản đã bị khóa");
        if (logout != null) m.addAttribute("msg", "Đã đăng xuất");
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") LoginRequest request, BindingResult result,
                        HttpServletRequest req, HttpServletResponse res, Model m) throws BindException {
        m.addAttribute("identity", request.getIdentity());
        if (result.hasErrors()) {
            if (ProblemSupport.isApi(req)) {
                throw new BindException(result);
            }
            m.addAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "login";
        }
        try {
            Authentication auth = authService.login(request, req, res);
            return "redirect:" + SecurityConfig.homeFor(auth);
        } catch (IllegalArgumentException e) {
            m.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/403")
    public String forbidden() { return "403"; }


    @GetMapping("/register")
    public String registerForm() { return "register"; }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request, BindingResult result,
                           Model m, RedirectAttributes ra, HttpServletRequest req, HttpServletResponse response)
            throws BindException {
        m.addAttribute("username", request.getUsername());
        m.addAttribute("email", request.getEmail());
        if (result.hasErrors()) {
            if (ProblemSupport.isApi(req)) {
                throw new BindException(result);
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "register";
        }
        try {
            service.register(request.getUsername(), request.getEmail(), request.getPassword(),
                    request.getClientTimezoneOffset());
            ra.addFlashAttribute("msg", "Đăng ký thành công, hãy đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            boolean duplicate = e.getMessage() != null
                    && (e.getMessage().contains("đã tồn tại") || e.getMessage().contains("đã được sử dụng"));
            int status = duplicate ? HttpServletResponse.SC_CONFLICT : HttpServletResponse.SC_BAD_REQUEST;
            log.warn("Register failed ({}): {}", status, e.getMessage());
            response.setStatus(status);
            m.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}
