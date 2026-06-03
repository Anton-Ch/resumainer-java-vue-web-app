package com.resumainer.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CsrfFilterTest {

    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @PostMapping("/api/test/submit")
        public String submit() {
            return "Submitted";
        }

        @PostMapping("/api/auth/login")
        public String login() {
            return "Logged in";
        }

        @GetMapping("/api/test/read")
        public String read() {
            return "Data";
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .addFilters(new CsrfFilter())
                .build();
    }

    @Test
    void postWithoutCsrfToken_returns403() throws Exception {
        mockMvc.perform(post("/api/test/submit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Invalid or missing CSRF token"));
    }

    @Test
    void getWithoutCsrfToken_returns200() throws Exception {
        mockMvc.perform(get("/api/test/read"))
                .andExpect(status().isOk())
                .andExpect(content().string("Data"));
    }

    @Test
    void postToExcludedPath_returns200WithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged in"));
    }

    @Test
    void postWithValidCsrfToken_returns200() throws Exception {
        // Create a session and pre-set a CSRF token
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("CSRF_TOKEN", "test-csrf-token-value");

        mockMvc.perform(post("/api/test/submit")
                        .session(session)
                        .header("X-CSRF-Token", "test-csrf-token-value")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Submitted"));
    }

    @Test
    void postWithWrongCsrfToken_returns403() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("CSRF_TOKEN", "real-token");

        mockMvc.perform(post("/api/test/submit")
                        .session(session)
                        .header("X-CSRF-Token", "wrong-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
