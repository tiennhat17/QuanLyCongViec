package com.ccnlthd.taskmanager.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Safety net for exceptions not handled locally in controllers.
 * Spring Security exceptions (AccessDenied, UsernameNotFound) are left to the security filter chain.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleBusiness(IllegalArgumentException e, Model m) {
        return error(m, HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public String handleBadRequest(Exception e, Model m) {
        return error(m, HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleConflict(DataIntegrityViolationException e, Model m) {
        log.warn("Data integrity violation", e);
        return error(m, HttpStatus.CONFLICT, "Dữ liệu đã tồn tại hoặc không hợp lệ");
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception e, Model m) {
        log.error("Unexpected error", e);
        return error(m, HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống");
    }

    private String error(Model m, HttpStatus status, String message) {
        m.addAttribute("status", status.value());
        m.addAttribute("message", message);
        return "error";
    }
}
