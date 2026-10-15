package com.ccnlthd.taskmanager.exception;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Shared helpers to build, log and write ProblemDetail (RFC 7807) error responses. */
public final class ProblemSupport {
    private ProblemSupport() {
    }

    /** API clients: /api/** paths or requests that explicitly prefer JSON over HTML. */
    public static boolean isApi(HttpServletRequest req) {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.startsWith("/api/")) {
            return true;
        }
        String accept = req.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)
                && !accept.contains(MediaType.TEXT_HTML_VALUE);
    }

    public static ProblemDetail build(HttpStatus status, String detail, HttpServletRequest req, String traceId) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(status.getReasonPhrase());
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("traceId", traceId);
        return pd;
    }

    public static String newTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static void log(Logger log, HttpStatus status, HttpServletRequest req, Throwable ex, String traceId) {
        String msg = "[{}] {} {} {} -> {} : {}";
        if (status.is5xxServerError()) {
            log.error(msg, traceId, status.value(), req.getMethod(), req.getRequestURI(),
                    ex.getClass().getSimpleName(), ex.getMessage(), ex);
        } else {
            log.warn(msg, traceId, status.value(), req.getMethod(), req.getRequestURI(),
                    ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    public static void write(HttpServletResponse resp, ProblemDetail pd) throws IOException {
        resp.setStatus(pd.getStatus());
        resp.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> props = pd.getProperties();
        String json = "{\"type\":" + quote(String.valueOf(pd.getType()))
                + ",\"title\":" + quote(pd.getTitle())
                + ",\"status\":" + pd.getStatus()
                + ",\"detail\":" + quote(pd.getDetail())
                + ",\"instance\":" + quote(String.valueOf(pd.getInstance()))
                + ",\"timestamp\":" + quote(String.valueOf(props.get("timestamp")))
                + ",\"traceId\":" + quote(String.valueOf(props.get("traceId"))) + "}";
        resp.getWriter().write(json);
    }

    private static String quote(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
