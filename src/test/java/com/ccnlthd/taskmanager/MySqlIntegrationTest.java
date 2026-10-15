package com.ccnlthd.taskmanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ccnlthd.taskmanager.model.Category;
import com.ccnlthd.taskmanager.model.Task;
import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.repository.CategoryRepository;
import com.ccnlthd.taskmanager.repository.TaskRepository;
import com.ccnlthd.taskmanager.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class MySqlIntegrationTest {
    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("task_manager_test")
            .withUsername("task_manager_test")
            .withPassword("task_manager_test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void configureMySql(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", MYSQL::getJdbcUrl);
        properties.add("spring.datasource.username", MYSQL::getUsername);
        properties.add("spring.datasource.password", MYSQL::getPassword);
        properties.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        properties.add("spring.flyway.enabled", () -> "true");
    }

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void flywayMigrationsAndHibernateValidationWorkOnMySql() {
        assertTrue(jdbcTemplate.queryForObject("select version()", String.class).startsWith("8.4"));
        assertEquals("3", flyway.info().current().getVersion().getVersion());
        assertEquals(3, flyway.info().applied().length);
    }

    @Test
    void helloEndpointAndDashboardWorkOnMySql() throws Exception {
        User account = createUser("mysql-dashboard");
        Category category = createCategory(account);
        LocalDate today = LocalDate.now();
        createTask(account, category, Task.Status.COMPLETED, today.minusDays(1));
        createTask(account, category, Task.Status.TODO, today.minusDays(1));
        createTask(account, category, Task.Status.TODO, today);
        createTask(account, category, Task.Status.TODO, today.plusDays(1));

        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("task-manager-api is running"));

        mockMvc.perform(get("/dashboard").with(user("mysql-dashboard").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("statistics",
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.hasProperty("totalTasks",
                                        org.hamcrest.Matchers.equalTo(4L)),
                                org.hamcrest.Matchers.hasProperty("completedTasks",
                                        org.hamcrest.Matchers.equalTo(1L)),
                                org.hamcrest.Matchers.hasProperty("pendingTasks",
                                        org.hamcrest.Matchers.equalTo(3L)),
                                org.hamcrest.Matchers.hasProperty("overdueTasks",
                                        org.hamcrest.Matchers.equalTo(1L)),
                                org.hamcrest.Matchers.hasProperty("dueTodayTasks",
                                        org.hamcrest.Matchers.equalTo(1L)))));
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("encoded");
        user.setRole(User.Role.USER);
        return userRepository.save(user);
    }

    private Category createCategory(User user) {
        Category category = new Category();
        category.setName("Công việc");
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
