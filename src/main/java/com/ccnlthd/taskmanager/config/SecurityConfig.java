package com.ccnlthd.taskmanager.config;

import jakarta.servlet.DispatcherType;

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

	public static String homeFor(Authentication auth) {
		boolean admin = auth.getAuthorities().stream()
				.anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
		return admin ? "/admin" : "/tasks";
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository userRepository) throws Exception {
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
						.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
						.accessDeniedPage("/403"))
				.build();
	}
}