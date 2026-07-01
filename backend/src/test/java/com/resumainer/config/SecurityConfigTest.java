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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Phase 1 — Tests that the non-Boot Spring Security bootstrap is active.
 *
 * <p>Verifies:
 * <ul>
 *   <li>{@code SecurityConfig} loads correctly with {@code @EnableWebSecurity}</li>
 *   <li>{@link SecurityFilterChain} bean is created</li>
 *   <li>{@link PasswordEncoder} bean is created</li>
 *   <li>{@link FilterChainProxy} (the {@code springSecurityFilterChain} internal bean)
 *       is registered</li>
 *   <li>MockMvc with {@link SecurityMockMvcConfigurers#springSecurity()} applies
 *       the filter chain</li>
 *   <li>Phase 1 permissive mode: endpoints pass through Spring Security without
 *       being challenged (no 401/403)</li>
 * </ul>
 *
 * <p><b>MockMvc caveat:</b> This test uses {@code webAppContextSetup} (not
 * {@code standaloneSetup}) to correctly apply the Spring Security filter chain.
 * {@code standaloneSetup} creates a fresh {@code MockHttpSession} per
 * {@code perform()} and does <b>not</b> apply security filters by default.
 * For Phase 1 permissive mode this is not a problem, but later phases that
 * require session state or authentication must be aware of this behavior.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SecurityConfig.class, SecurityConfigTest.TestSecurityConfig.class})
@WebAppConfiguration
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired(required = false)
    private SecurityFilterChain securityFilterChain;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private FilterChainProxy filterChainProxy;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("SecurityFilterChain bean is created by SecurityConfig")
    void securityFilterChainBean_exists() {
        assertNotNull(securityFilterChain,
                "SecurityFilterChain bean must be defined in SecurityConfig");
    }

    @Test
    @DisplayName("PasswordEncoder bean is created by SecurityConfig")
    void passwordEncoderBean_exists() {
        assertNotNull(passwordEncoder,
                "PasswordEncoder bean must be defined in SecurityConfig");
    }

    @Test
    @DisplayName("FilterChainProxy (springSecurityFilterChain) is registered")
    void filterChainProxy_exists() {
        assertNotNull(filterChainProxy,
                "FilterChainProxy (springSecurityFilterChain) must be created by @EnableWebSecurity");
    }

    @Test
    @DisplayName("Phase 7: GET any-path without auth returns 403 (authenticated required)")
    void getRequest_withoutAuth_returnsForbidden() throws Exception {
        // Phase 7 changed from permitAll to .anyRequest().authenticated(),
        // so unauthenticated requests return 403 Forbidden.
        mockMvc.perform(get("/any-path"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Phase 7: POST any-path with CSRF but without auth returns 403")
    void postRequest_withoutAuth_returnsForbidden() throws Exception {
        mockMvc.perform(post("/any-path")
                        .contentType("application/json")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Phase 6: POST any-path without CSRF returns 403")
    void postWithoutCsrf_returns403() throws Exception {
        mockMvc.perform(post("/any-path")
                        .contentType("application/json"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Phase 1 permissive: GET /api/auth/status passes security")
    void authStatusPath_passesThroughSecurity() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PasswordEncoder produces valid BCrypt hash")
    void passwordEncoder_producesValidBcryptHash() {
        String hash = passwordEncoder.encode("TestPass123");
        assertNotNull(hash, "BCrypt hash must not be null");
        assertTrue(hash.startsWith("$2a$"),
                "BCrypt hash must start with $2a$ but got: " + hash);
        assertTrue(passwordEncoder.matches("TestPass123", hash),
                "PasswordEncoder must verify correct password");
        assertTrue(passwordEncoder.matches("WrongPass", hash) == false,
                "PasswordEncoder must reject wrong password");
    }

    /**
     * Test configuration that provides a mock {@link UserDao} for the
     * {@code userDetailsService} bean defined in {@link SecurityConfig}.
     */
    @Configuration
    static class TestSecurityConfig {
        @Bean
        public UserDao userDao() {
            return Mockito.mock(UserDao.class);
        }

        /**
         * Required by MvcRequestMatcher in SecurityConfig.filterChain().
         * Without this bean, Spring Security fails to parse requestMatchers
         * in non-Boot test contexts that do not load @EnableWebMvc.
         */
        @Bean
        public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
            return new HandlerMappingIntrospector();
        }
    }
}
