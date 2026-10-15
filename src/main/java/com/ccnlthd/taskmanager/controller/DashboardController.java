package com.ccnlthd.taskmanager.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.service.TaskService;
import com.ccnlthd.taskmanager.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final UserService userService;
    private final TaskService taskService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("statistics", taskService.getStatisticsForUser(user.getId()));
        return "dashboard";
    }
}
