package com.ccnlthd.taskmanager.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
public class AccountController {
    private final UserService userService;
    private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    @GetMapping("/account")
    public String account(Authentication authentication, Model model) {
        model.addAttribute("account", userService.getAccount(authentication.getName()));
        model.addAttribute("homeUrl", SecurityConfig.homeFor(authentication));
        return "account";
    }

    @PostMapping("/account/profile")
    public String updateProfile(@RequestParam String username,
                                @RequestParam String email,
                                Authentication authentication,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                RedirectAttributes redirectAttributes) {
        try {
            User account = userService.updateProfile(authentication.getName(), username, email);
            if (!account.getUsername().equals(authentication.getName())) {
                Authentication updatedAuthentication = UsernamePasswordAuthenticationToken.authenticated(
                        account.getUsername(), authentication.getCredentials(), authentication.getAuthorities());
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(updatedAuthentication);
                SecurityContextHolder.setContext(context);
                contextRepository.saveContext(context, request, response);
            }
            redirectAttributes.addFlashAttribute("msg", "Đã cập nhật thông tin cá nhân.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/account";
    }

    @PostMapping("/account/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.changePassword(authentication.getName(), currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("msg", "Đã đổi mật khẩu.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/account";
    }
}