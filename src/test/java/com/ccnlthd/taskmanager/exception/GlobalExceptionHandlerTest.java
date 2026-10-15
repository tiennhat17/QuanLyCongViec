package com.ccnlthd.taskmanager.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

    @RestController
    static class Boom {
        @GetMapping("/api/bad")
        String bad() {
            throw new IllegalArgumentException("sai");
        }

        @GetMapping("/api/missing")
        String missing() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        @GetMapping("/api/conflict")
        String conflict() {
            throw new DataIntegrityViolationException("dup");
        }

        @GetMapping("/api/boom")
        String boom() {
            throw new IllegalStateException("x");
        }
    }

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new Boom()).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void badRequestIsProblemDetail() throws Exception {
        mvc.perform(get("/api/bad")).andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.detail").value("sai"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void notFoundConflictAndServerError() throws Exception {
        mvc.perform(get("/api/missing")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
        mvc.perform(get("/api/conflict")).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
        mvc.perform(get("/api/boom")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
