package com.ccnlthd.taskmanager.config;

import jakarta.servlet.DispatcherType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import com.ccnlthd.taskmanager.exception.ProblemSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import com.ccnlthd.taskmanager.repository.UserRepository;
import com.ccnlthd.taskmanager.security.LockedUserSessionFilter;

@Configuration
public class SecurityConfig {
	private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	public static String homeFor(Authentication auth) {
		boolean admin = auth.getAuthorities().stream()
				.anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
		return admin ? "/admin" : "/tasks";
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository userRepository) throws Exception {
		LoginUrlAuthenticationEntryPoint browserEntry = new LoginUrlAuthenticationEntryPoint("/login");
		AccessDeniedHandlerImpl browserDenied = new AccessDeniedHandlerImpl();
		browserDenied.setErrorPage("/403");
		return http
				.authorizeHttpRequests(requests -> requests
						.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
						.requestMatchers(
							"/api/hello", 
							"/actuator/health", 
							"/login", 
							"/register", 
							"/403",
							"/error",
                            "/css/**", 
							"/js/**", 
							"/images/**").permitAll()
						.requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
						.requestMatchers("/tasks", "/tasks/**").hasRole("USER")
						.anyRequest().authenticated())
				// POST /login is handled by AuthController, so form login is not used
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout"))
				.addFilterBefore(new LockedUserSessionFilter(userRepository), AuthorizationFilter.class)
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((req, res, e) -> {
							if (ProblemSupport.isApi(req)) {
								String id = ProblemSupport.newTraceId();
								ProblemSupport.log(log, HttpStatus.UNAUTHORIZED, req, e, id);
								ProblemSupport.write(res, ProblemSupport.build(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập", req, id));
							} else {
								browserEntry.commence(req, res, e);
							}
						})
						.accessDeniedHandler((req, res, e) -> {
							if (ProblemSupport.isApi(req)) {
								String id = ProblemSupport.newTraceId();
								ProblemSupport.log(log, HttpStatus.FORBIDDEN, req, e, id);
								ProblemSupport.write(res, ProblemSupport.build(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập", req, id));
							} else {
								log.warn("403 {} {} -> {}", req.getMethod(), req.getRequestURI(), e.getMessage());
								browserDenied.handle(req, res, e);
							}
						}))
				.build();
	}
}