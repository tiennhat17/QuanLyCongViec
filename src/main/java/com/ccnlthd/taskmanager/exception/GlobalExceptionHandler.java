package com.ccnlthd.taskmanager.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unified error handling: API requests get ProblemDetail (RFC 7807) JSON,
 * browser requests get the "error" view, both with the correct HTTP status.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleBusiness(IllegalArgumentException e, HttpServletRequest req, HttpServletResponse res) {
        return respond(HttpStatus.BAD_REQUEST, e.getMessage(), null, e, req, res);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public Object handleBadRequest(Exception e, HttpServletRequest req, HttpServletResponse res) {
        return respond(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ", null, e, req, res);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Object handleValidation(BindException e, HttpServletRequest req, HttpServletResponse res) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return respond(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", fields, e, req, res);
    }

    @ExceptionHandler(AuthenticationException.class)
    public Object handleUnauthorized(AuthenticationException e, HttpServletRequest req, HttpServletResponse res) {
        return respond(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập", null, e, req, res);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleForbidden(AccessDeniedException e, HttpServletRequest req, HttpServletResponse res) {
        return respond(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập", null, e, req, res);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResource(Exception e, HttpServletRequest req, HttpServletResponse res) {
        return respond(HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên", null, e, req, res);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Object handleStatus(ResponseStatusException e, HttpServletRequest req, HttpServletResponse res) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String msg = e.getReason() != null ? e.getReason()
                : status == HttpStatus.NOT_FOUND ? "Không tìm thấy tài nguyên" : status.getReasonPhrase();
        return respond(status, msg, null, e, req, res);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethod(HttpRequestMethodNotSupportedException e, HttpServletRequest req,
            HttpServletResponse res) {
        return respond(HttpStatus.METHOD_NOT_ALLOWED, "Phương thức không được hỗ trợ", null, e, req, res);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object handleConflict(DataIntegrityViolationException e, HttpServletRequest req,
            HttpServletResponse res) {
        return respond(HttpStatus.CONFLICT, "Dữ liệu đã tồn tại hoặc không hợp lệ", null, e, req, res);
    }

    @ExceptionHandler(Exception.class)
    public Object handleUnexpected(Exception e, HttpServletRequest req, HttpServletResponse res) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống", null, e, req, res);
    }

    private Object respond(HttpStatus status, String message, Map<String, String> fields, Exception e,
            HttpServletRequest req, HttpServletResponse res) {
        String traceId = ProblemSupport.newTraceId();
        ProblemSupport.log(log, status, req, e, traceId);
        if (ProblemSupport.isApi(req)) {
            ProblemDetail pd = ProblemSupport.build(status, message, req, traceId);
            if (fields != null && !fields.isEmpty()) {
                pd.setProperty("errors", fields);
            }
            return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(pd);
        }
        res.setStatus(status.value());
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("status", status.value());
        mv.addObject("message", message);
        mv.addObject("traceId", traceId);
        return mv;
    }
}
