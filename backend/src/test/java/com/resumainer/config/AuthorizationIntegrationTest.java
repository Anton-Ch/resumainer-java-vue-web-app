package com.resumainer.config;

import com.resumainer.dao.UserDao;
import com.resumainer.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Phase 7 — Integration tests for Spring Security authorization rules.
 *
 * <p>Verifies that {@link SecurityConfig} authorization rules correctly enforce
 * access control after the legacy {@code AuthInterceptor} is removed.
 *
 * <p>Uses explicit AntPathRequestMatcher / RegexRequestMatcher in SecurityConfig
 * (not string-based MvcRequestMatcher), so no HandlerMappingIntrospector bean
 * is needed in the test context.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SecurityConfig.class, AuthorizationIntegrationTest.TestConfig.class})
@WebAppConfiguration
class AuthorizationIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private static UserDao mockUserDao = Mockito.mock(UserDao.class);

    @BeforeEach
    void setUp() {
        Mockito.reset(mockUserDao);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ============================================================
    // Public endpoints: landing, static, public resume
    // ============================================================

    @Test
    @DisplayName("GET / (landing page) is accessible without auth")
    void landingPage_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isNotFound()); // 404 = passed security, no controller
    }

    @Test
    @DisplayName("GET /static/css/style.css is accessible without auth")
    void staticAsset_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/static/css/style.css"))
                .andExpect(status().isNotFound()); // 404 = passed security
    }

    @Test
    @DisplayName("GET /error/404 is accessible without auth")
    void errorPage_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/error/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /favicon.ico is accessible without auth")
    void favicon_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Public resume URL /someuser/ABC123 is accessible without auth")
    void publicResumeUrl_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/someuser/ABC123"))
                .andExpect(status().isNotFound()); // 404 = passed security, controller not in test
    }

    @Test
    @DisplayName("Public resume URL with dots and hyphens is accessible without auth")
    void publicResumeUrl_complexUsername_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/john.doe_123/X7k9M2"))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // CORS preflight — OPTIONS must not be blocked
    // ============================================================

    @Test
    @DisplayName("OPTIONS /api/profile/contact without auth is not rejected by authorization")
    void optionsPreflight_isNotBlockedByAuthorization() throws Exception {
        mockMvc.perform(options("/api/profile/contact")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isNotFound()); // 404 = passed security, no controller
    }

    // ============================================================
    // T080 — Unauthenticated rejected from protected APIs
    // ============================================================

    @Test
    @DisplayName("T080: GET /api/profile without auth is rejected (403)")
    void unauthenticatedRequestToProfileEndpoint_isRejected() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("T080: POST to protected API without auth is rejected (403)")
    void unauthenticatedPostToProtectedApi_isRejected() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/profile")
                        .contentType("application/json")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // T081 — USER rejected from /api/admin/**
    // ============================================================

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    @DisplayName("T081: GET /api/admin/dashboard as USER returns 403")
    void userAccessToAdminEndpoint_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    @DisplayName("T081: POST /api/admin/manage as USER returns 403")
    void userPostToAdminEndpoint_returnsForbidden() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/admin/manage")
                        .contentType("application/json")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // T082 — ADMIN allowed to /api/admin/**
    // ============================================================

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("T082: GET /api/admin/dashboard as ADMIN passes (404 = no controller)")
    void adminAccessToAdminEndpoint_isAllowed() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isNotFound()); // 404 = passed security
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @DisplayName("T082: POST /api/admin/manage as ADMIN passes (404 = no controller)")
    void adminPostToAdminEndpoint_isAllowed() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/admin/manage")
                        .contentType("application/json")
                        .with(csrf()))
                .andExpect(status().isNotFound()); // 404 = passed security
    }

    // ============================================================
    // Public auth endpoints remain accessible without auth
    // ============================================================

    @Test
    @DisplayName("GET /api/auth/status is accessible without auth")
    void publicAuthEndpoint_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isNotFound()); // 404 = passed security
    }

    @Test
    @DisplayName("GET /api/csrf is accessible without auth")
    void csrfEndpoint_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isNotFound()); // 404 = passed security
    }

    // ============================================================
    // Authenticated user can access protected API
    // ============================================================

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    @DisplayName("Authenticated USER can access /api/profile (404 = no controller)")
    void authenticatedUser_canAccessProtectedApi() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isNotFound()); // 404 = passed security
    }

    // ============================================================
    // Test Configuration
    // ============================================================

    @Configuration
    static class TestConfig {

        @Bean
        public UserDao userDao() {
            return mockUserDao;
        }
    }
}
