package com.ccnlthd.taskmanager.service;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import com.ccnlthd.taskmanager.dto.request.LoginRequest;
import com.ccnlthd.taskmanager.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final SecurityContextRepository contextRepo = new HttpSessionSecurityContextRepository();

    /** Verifies credentials and establishes the authenticated security context in the session. */
    public Authentication login(LoginRequest request, HttpServletRequest req, HttpServletResponse res) {
        User u = userService.login(request.getIdentity(), request.getPassword());
        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
                u.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
        if (req.getSession(false) != null) req.changeSessionId(); // chống session fixation
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        contextRepo.saveContext(ctx, req, res);
        return auth;
    }
}
