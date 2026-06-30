package com.resumainer.config;

import com.resumainer.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextConfiguration(classes = SecurityConfig.class)
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
    @DisplayName("Phase 1 permissive: GET any-path passes security (returns 404 — no controller)")
    void getRequest_passesThroughSecurity() throws Exception {
        // 404 is expected because there is no controller mapped to /any-path,
        // but the fact that we get 404 (not 401/403) proves Spring Security
        // permitted the request in Phase 1 permissive mode.
        mockMvc.perform(get("/any-path"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Phase 1 permissive: POST any-path passes security (returns 404 — no controller)")
    void postRequest_passesThroughSecurity() throws Exception {
        mockMvc.perform(post("/any-path")
                        .contentType("application/json"))
                .andExpect(status().isNotFound());
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
}
