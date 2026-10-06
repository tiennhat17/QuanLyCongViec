package com.ccnlthd.taskmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ccnlthd.taskmanager.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private static final int USERS_PER_PAGE = 10;
    private final UserService userService;

    @GetMapping("/admin")
    public String dashboard(Model model) {
        var statistics = userService.getAdminUserStatistics();
        model.addAttribute("statistics", statistics);
        return "admin";
    }

    @GetMapping("/admin/users")
    public String users(@RequestParam(defaultValue = "") String search,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        int pageNumber = Math.max(page, 0);
        Page<?> users = userService.findUsers(search,
                PageRequest.of(pageNumber, USERS_PER_PAGE,
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        model.addAttribute("page", pageNumber);
        return "admin-users";
    }

    @PostMapping("/admin/users/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam boolean enabled,
                               @RequestParam(defaultValue = "") String search,
                               @RequestParam(defaultValue = "0") int page,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.updateStatus(id, authentication.getName(), enabled);
            redirectAttributes.addFlashAttribute("msg",
                    enabled ? "Đã mở khóa tài khoản." : "Đã khóa tài khoản.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        String suffix = "?page=" + Math.max(page, 0);
        if (!search.isBlank()) {
            suffix += "&search=" + java.net.URLEncoder.encode(
                    search, java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/admin/users" + suffix;
    }
}
