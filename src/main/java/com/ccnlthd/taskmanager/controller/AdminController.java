package com.ccnlthd.taskmanager.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ccnlthd.taskmanager.dto.AdminDashboardDTO;
import com.ccnlthd.taskmanager.dto.AdminUserDTO;
import com.ccnlthd.taskmanager.service.AdminDashboardService;
import com.ccnlthd.taskmanager.service.AdminUserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private static final int USERS_PER_PAGE = 10;
    private final AdminDashboardService adminDashboardService;
    private final AdminUserService adminUserService;

    @GetMapping("/admin")
    public String dashboard(Model model) {
        AdminDashboardDTO dashboard = adminDashboardService.getAdminDashboard();
        model.addAttribute("dashboard", dashboard);
        return "admin";
    }

    @GetMapping("/admin/tasks")
    public String taskStatistics(Model model) {
        AdminDashboardDTO dashboard = adminDashboardService.getAdminDashboard();
        model.addAttribute("statistics", dashboard);
        return "admin-tasks";
    }

    @GetMapping("/admin/users")
    public String users(@RequestParam(defaultValue = "") String search,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        int pageNumber = Math.max(page, 0);
        Page<AdminUserDTO> users = adminUserService.findUsers(search,
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
            adminUserService.updateStatus(id, authentication.getName(), enabled);
            redirectAttributes.addFlashAttribute("msg",
                    enabled ? "Đã mở khóa tài khoản." : "Đã khóa tài khoản.");
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        String suffix = "?page=" + Math.max(page, 0);
        if (!search.isBlank()) {
            suffix += "&search=" + java.net.URLEncoder.encode(
                    search, java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/admin/users" + suffix;
    }
}
