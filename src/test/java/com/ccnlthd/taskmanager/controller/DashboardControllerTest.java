package com.ccnlthd.taskmanager.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ccnlthd.taskmanager.model.Category;
import com.ccnlthd.taskmanager.model.Task;
import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.repository.CategoryRepository;
import com.ccnlthd.taskmanager.repository.TaskRepository;
import com.ccnlthd.taskmanager.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void dashboardShowsTaskStatisticsOnlyForCurrentUser() throws Exception {
        User user = createUser("dashboard-user");
        Category category = createCategory(user, "Công việc");
        LocalDate today = LocalDate.now();

        createTask(user, category, Task.Status.COMPLETED, today.minusDays(2));
        createTask(user, category, Task.Status.TODO, today.minusDays(1));
        createTask(user, category, Task.Status.TODO, today);
        createTask(user, category, Task.Status.TODO, today.plusDays(1));
        createTask(user, category, Task.Status.TODO, null);

        User anotherUser = createUser("another-user");
        Category anotherCategory = createCategory(anotherUser, "Khác");
        createTask(anotherUser, anotherCategory, Task.Status.TODO, today);

        mockMvc.perform(get("/dashboard").with(user("dashboard-user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("statistics",
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.hasProperty("totalTasks",
                                        org.hamcrest.Matchers.equalTo(5L)),
                                org.hamcrest.Matchers.hasProperty("completedTasks",
                                        org.hamcrest.Matchers.equalTo(1L)),
                                org.hamcrest.Matchers.hasProperty("pendingTasks",
                                        org.hamcrest.Matchers.equalTo(4L)),
                                org.hamcrest.Matchers.hasProperty("overdueTasks",
                                        org.hamcrest.Matchers.equalTo(1L)),
                                org.hamcrest.Matchers.hasProperty("dueTodayTasks",
                                        org.hamcrest.Matchers.equalTo(1L)))));
    }

    @Test
    void dashboardRequiresUserRole() throws Exception {
        mockMvc.perform(get("/dashboard").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("encoded");
        user.setRole(User.Role.USER);
        return userRepository.save(user);
    }

    private Category createCategory(User user, String name) {
        Category category = new Category();
        category.setName(name);
        category.setUser(user);
        return categoryRepository.save(category);
    }

    private void createTask(User user, Category category, Task.Status status, LocalDate dueDate) {
        Task task = new Task();
        task.setTitle("Task " + status + " " + dueDate);
        task.setUser(user);
        task.setCategory(category);
        task.setStatus(status);
        task.setDueDate(dueDate);
        taskRepository.save(task);
    }
}
