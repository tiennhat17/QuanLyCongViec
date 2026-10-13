package com.ccnlthd.taskmanager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ccnlthd.taskmanager.model.User;
import com.ccnlthd.taskmanager.model.Category;
import com.ccnlthd.taskmanager.model.Task;
import com.ccnlthd.taskmanager.repository.CategoryRepository;
import com.ccnlthd.taskmanager.repository.TaskRepository;
import com.ccnlthd.taskmanager.repository.UserRepository;
import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskManagerApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void cleanUsers() {
		taskRepository.deleteAll();
		categoryRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void helloEndpointReturnsJson() throws Exception {
		mockMvc.perform(get("/api/hello"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("task-manager-api is running"));
	}

	@Test
	void adminCanViewUsersAndFilterByUsername() throws Exception {
		User user = new User();
		user.setUsername("nguyenan");
		user.setEmail("an@example.com");
		user.setPassword("encoded");
		user.setRole(User.Role.USER);
		userRepository.save(user);

		mockMvc.perform(get("/admin/users")
				.param("search", "nguyen")
				.with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin-users"))
				.andExpect(model().attribute("users",
						org.hamcrest.Matchers.hasProperty("totalElements", org.hamcrest.Matchers.equalTo(1L))));
	}

	@Test
	void adminCanViewUserDashboardStatistics() throws Exception {
		User active = new User();
		active.setUsername("active");
		active.setEmail("active@example.com");
		active.setPassword("encoded");
		active.setRole(User.Role.USER);
		userRepository.save(active);

		User locked = new User();
		locked.setUsername("locked-dashboard");
		locked.setEmail("locked-dashboard@example.com");
		locked.setPassword("encoded");
		locked.setRole(User.Role.USER);
		locked.setStatus(false);
		userRepository.save(locked);

		mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin"))
				.andExpect(model().attributeExists("statistics"));
	}

	@Test
	void adminDashboardIncludesTaskAndCategoryStatistics() throws Exception {
		User user = createUser("stats-user", "stats@example.com");
		user = userRepository.save(user);
		Category category = new Category();
		category.setName("Work");
		category.setUser(user);
		category = categoryRepository.save(category);

		Task completed = task(user, category, Task.Status.COMPLETED, LocalDate.now());
		Task overdue = task(user, category, Task.Status.TODO, LocalDate.now().minusDays(1));
		taskRepository.saveAll(java.util.List.of(completed, overdue));

		mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(model().attribute("statistics",
						org.hamcrest.Matchers.allOf(
								org.hamcrest.Matchers.hasProperty("totalTasks", org.hamcrest.Matchers.equalTo(2L)),
								org.hamcrest.Matchers.hasProperty("completedTasks", org.hamcrest.Matchers.equalTo(1L)),
								org.hamcrest.Matchers.hasProperty("pendingTasks", org.hamcrest.Matchers.equalTo(1L)),
								org.hamcrest.Matchers.hasProperty("overdueTasks", org.hamcrest.Matchers.equalTo(1L)),
								org.hamcrest.Matchers.hasProperty("totalCategories", org.hamcrest.Matchers.equalTo(1L)))));
	}

	@Test
	void adminUserListIncludesTaskCountPerUser() throws Exception {
		User user = userRepository.save(createUser("task-owner", "task-owner@example.com"));
		Category category = new Category();
		category.setName("Work");
		category.setUser(user);
		category = categoryRepository.save(category);
		taskRepository.save(task(user, category, Task.Status.TODO, LocalDate.now()));
		taskRepository.save(task(user, category, Task.Status.COMPLETED, LocalDate.now()));

		mockMvc.perform(get("/admin/users").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(model().attribute("users",
						org.hamcrest.Matchers.hasProperty("content",
								org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.allOf(
										org.hamcrest.Matchers.hasProperty("id",
												org.hamcrest.Matchers.equalTo(user.getId())),
										org.hamcrest.Matchers.hasProperty("taskCount",
												org.hamcrest.Matchers.equalTo(2L)))))));
	}

	@Test
	void adminUserListPaginatesTenUsersPerPage() throws Exception {
		for (int i = 1; i <= 21; i++) {
			User user = new User();
			user.setUsername(String.format("user%02d", i));
			user.setEmail(String.format("user%02d@example.com", i));
			user.setPassword("encoded");
			user.setRole(User.Role.USER);
			userRepository.save(user);
		}

		mockMvc.perform(get("/admin/users").param("page", "0")
				.with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(model().attribute("users",
						org.hamcrest.Matchers.hasProperty("numberOfElements",
								org.hamcrest.Matchers.equalTo(10))))
				.andExpect(model().attribute("users",
						org.hamcrest.Matchers.hasProperty("totalPages",
								org.hamcrest.Matchers.equalTo(3))));

		mockMvc.perform(get("/admin/users").param("page", "2")
				.with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(model().attribute("users",
						org.hamcrest.Matchers.hasProperty("numberOfElements",
								org.hamcrest.Matchers.equalTo(1))));
	}

	@Test
	void regularUserCannotViewAdminUsers() throws Exception {
		mockMvc.perform(get("/admin/users").with(user("user").roles("USER")))
				.andExpect(status().isForbidden());
	}

	private User createUser(String username, String email) {
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword("encoded");
		user.setRole(User.Role.USER);
		return user;
	}

	private Task task(User user, Category category, Task.Status status, LocalDate dueDate) {
		Task task = new Task();
		task.setTitle("Task");
		task.setUser(user);
		task.setCategory(category);
		task.setStatus(status);
		task.setDueDate(dueDate);
		return task;
	}

	@Test
	void adminCanLockAndUnlockAnotherUser() throws Exception {
		User target = new User();
		target.setUsername("target");
		target.setEmail("target@example.com");
		target.setPassword("encoded");
		target.setRole(User.Role.USER);
		target = userRepository.save(target);

		mockMvc.perform(post("/admin/users/" + target.getId() + "/status")
				.param("enabled", "false")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
				.andExpect(status().is3xxRedirection());
		User locked = userRepository.findById(target.getId()).orElseThrow();
		org.junit.jupiter.api.Assertions.assertFalse(locked.isStatus());

		mockMvc.perform(post("/admin/users/" + target.getId() + "/status")
				.param("enabled", "true")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
				.andExpect(status().is3xxRedirection());
		org.junit.jupiter.api.Assertions.assertTrue(userRepository.findById(target.getId()).orElseThrow().isStatus());
	}

	@Test
	void adminCannotChangeOwnStatus() throws Exception {
		User admin = new User();
		admin.setUsername("admin");
		admin.setEmail("admin@example.com");
		admin.setPassword("encoded");
		admin.setRole(User.Role.ADMIN);
		admin = userRepository.save(admin);

		mockMvc.perform(post("/admin/users/" + admin.getId() + "/status")
				.param("enabled", "false")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
				.andExpect(status().is3xxRedirection());

		org.junit.jupiter.api.Assertions.assertTrue(userRepository.findById(admin.getId()).orElseThrow().isStatus());
	}

	@Test
	void lockedUserCannotLogIn() throws Exception {
		User locked = new User();
		locked.setUsername("locked");
		locked.setEmail("locked@example.com");
		locked.setPassword(passwordEncoder.encode("password"));
		locked.setRole(User.Role.USER);
		locked.setStatus(false);
		userRepository.save(locked);

		mockMvc.perform(post("/login")
				.param("identity", "locked")
				.param("password", "password")
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("login"))
				.andExpect(model().attribute("error", "Tài khoản đã bị khóa"));
	}

	@Test
	void lockingUserInvalidatesTheirNextAuthenticatedRequest() throws Exception {
		User locked = new User();
		locked.setUsername("locked-session");
		locked.setEmail("locked-session@example.com");
		locked.setPassword("encoded");
		locked.setRole(User.Role.USER);
		locked.setStatus(false);
		userRepository.save(locked);

		mockMvc.perform(get("/tasks").with(user("locked-session").roles("USER")))
				.andExpect(status().is3xxRedirection())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
						.redirectedUrl("/login?locked"));
	}

	@Test
	void userCanViewAndUpdateTheirAccountProfile() throws Exception {
		User account = new User();
		account.setUsername("profile-user");
		account.setEmail("profile@example.com");
		account.setPassword(passwordEncoder.encode("old-password"));
		account.setRole(User.Role.USER);
		userRepository.save(account);

		mockMvc.perform(get("/account").with(user("profile-user").roles("USER")))
				.andExpect(status().isOk())
				.andExpect(view().name("account"))
				.andExpect(model().attributeExists("account"));

		mockMvc.perform(post("/account/profile")
				.param("username", "profile-updated")
				.param("email", "updated@example.com")
				.with(user("profile-user").roles("USER"))
				.with(csrf()))
				.andExpect(status().is3xxRedirection());

		User updated = userRepository.findById(account.getId()).orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals("profile-updated", updated.getUsername());
		org.junit.jupiter.api.Assertions.assertEquals("updated@example.com", updated.getEmail());
	}

	@Test
	void userCanChangeTheirPassword() throws Exception {
		User account = new User();
		account.setUsername("password-user");
		account.setEmail("password@example.com");
		account.setPassword(passwordEncoder.encode("old-password"));
		account.setRole(User.Role.USER);
		userRepository.save(account);

		mockMvc.perform(post("/account/password")
				.param("currentPassword", "old-password")
				.param("newPassword", "new-password")
				.param("confirmPassword", "new-password")
				.with(user("password-user").roles("USER"))
				.with(csrf()))
				.andExpect(status().is3xxRedirection());

		User updated = userRepository.findById(account.getId()).orElseThrow();
		org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("new-password", updated.getPassword()));
	}

}
