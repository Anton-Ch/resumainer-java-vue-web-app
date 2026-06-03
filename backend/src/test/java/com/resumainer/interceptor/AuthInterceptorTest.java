package com.resumainer.interceptor;

import com.resumainer.dto.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthInterceptorTest {

    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/api/users/me")
        public String protectedEndpoint() {
            return "OK";
        }

        @GetMapping("/api/auth/login")
        public String authEndpoint() {
            return "Login page";
        }
    }

    @BeforeEach
    void setUp() {
        AuthInterceptor interceptor = new AuthInterceptor();
        mockMvc = standaloneSetup(new TestController())
                .addInterceptors(interceptor)
                .build();
    }

    @Test
    void requestWithoutSession_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void requestWithSession_returns200() throws Exception {
        UserSession userSession = new UserSession(
                UUID.randomUUID(), "test@example.com", "USER");

        mockMvc.perform(get("/api/users/me")
                        .sessionAttr("user", userSession)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

}
