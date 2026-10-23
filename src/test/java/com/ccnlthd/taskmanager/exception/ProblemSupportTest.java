package com.ccnlthd.taskmanager.exception;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ProblemSupportTest {

    @Test
    void apiPathsAreDetected() {
        assertTrue(ProblemSupport.isApi(new MockHttpServletRequest("GET", "/api")));
        assertTrue(ProblemSupport.isApi(new MockHttpServletRequest("GET", "/api/users/1")));
    }

    @Test
    void browserPathsAreNotApi() {
        assertFalse(ProblemSupport.isApi(new MockHttpServletRequest("GET", "/tasks")));
        assertFalse(ProblemSupport.isApi(new MockHttpServletRequest("GET", "/apix")));
    }

    @Test
    void jsonAcceptIsApi() {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/tasks");
        req.addHeader("Accept", "application/json");
        assertTrue(ProblemSupport.isApi(req));
    }
}
