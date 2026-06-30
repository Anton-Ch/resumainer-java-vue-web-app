package com.resumainer.security;

import com.resumainer.dao.UserDao;
import com.resumainer.service.security.CustomUserDetailsService;
import com.resumainer.service.security.JsonAuthenticationFailureHandler;
import com.resumainer.service.security.JsonAuthenticationFilter;
import com.resumainer.service.security.JsonAuthenticationSuccessHandler;
import com.resumainer.service.security.JsonLogoutSuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Phase 1 — Minimal non-Boot Spring Security configuration.
 *
 * <p><b>TEMPORARY Phase 1 state:</b>
 * <ul>
 *   <li>All endpoints are {@code permitAll()} — auth rules will be added in Phase 4/7.</li>
 *   <li>Spring Security CSRF is {@code disabled} — legacy {@code CsrfFilter} remains active.
 *       Final CSRF migration will happen in Phase 6.</li>
 *   <li>Security headers are {@code disabled} — the existing {@code securityHeadersFilter}
 *       in {@code WebConfig} continues to provide headers.</li>
 * </ul>
 *
 * <p>This config is loaded in the ROOT {@code WebApplicationContext} via
 * {@code AppInitializer.getRootConfigClasses()}. The servlet context
 * {@code @ComponentScan} excludes this package to prevent duplicate
 * {@code @EnableWebSecurity} processing.
 *
 * <p>Non-Boot filter registration: the {@code springSecurityFilterChain} filter
 * is registered through {@code DelegatingFilterProxy} in
 * {@code AppInitializer.getServletFilters()} (not Spring Boot's {@code FilterRegistrationBean}).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * Phase 1 — Permissive filter chain.
     *
     * <p>All requests are permitted. Spring Security CSRF is disabled so that the
     * legacy {@code CsrfFilter} continues to provide CSRF protection unchanged.
     * Security headers are disabled so the existing {@code securityHeadersFilter} bean
     * in {@code WebConfig} remains the source of security header behavior.
     *
     * <p>Intended to be tightened in later phases:
     * <ul>
     *   <li>Phase 4 — JSON login/logout/status with Spring Security Authentication</li>
     *   <li>Phase 6 — Spring Security CSRF replaces legacy CsrfFilter</li>
     *   <li>Phase 7 — Authorization rules for ADMIN/USER routes</li>
     * </ul>
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authenticationManager,
                                           UserDao userDao) throws Exception {
        log.info("Phase 5 — Configuring Spring Security with failed login tracking");

        http
            // Phase 1+: all endpoints open — auth rules come in Phase 7
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // Phase 1+: Spring Security CSRF disabled — legacy CsrfFilter handles CSRF
            .csrf(AbstractHttpConfigurer::disable)
            // Phase 1: headers disabled — WebConfig.securityHeadersFilter continues
            .headers(headers -> headers.disable())
            // Phase 4: session management with session fixation protection
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
            );

        // Phase 4/5: JSON login filter with failed login counter tracking
        JsonAuthenticationFilter jsonFilter = new JsonAuthenticationFilter();
        jsonFilter.setAuthenticationManager(authenticationManager);
        jsonFilter.setAuthenticationSuccessHandler(new JsonAuthenticationSuccessHandler(userDao));
        jsonFilter.setAuthenticationFailureHandler(new JsonAuthenticationFailureHandler(userDao));
        http.addFilterAt(jsonFilter, UsernamePasswordAuthenticationFilter.class);

        // Phase 4: JSON logout
        http.logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(new JsonLogoutSuccessHandler())
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
        );

        SecurityFilterChain chain = http.build();
        log.info("Phase 5 — Spring Security filter chain built successfully");
        return chain;
    }

    /**
     * Spring Security {@code PasswordEncoder} bean.
     *
     * <p>Will replace the existing manual BCrypt usage in {@code PasswordService}
     * in Phase 3. Both can coexist during migration — the favre BCrypt library
     * and Spring Security's {@code BCryptPasswordEncoder} produce compatible hashes.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Spring Security {@code UserDetailsService} bean.
     *
     * <p>Loads users by email (not username). Integrates with the project's
     * {@code UserDao} which is available in the root context.
     * Spring Security automatically discovers this bean and uses it with
     * {@code DaoAuthenticationProvider} for authentication.
     */
    @Bean
    public UserDetailsService userDetailsService(UserDao userDao) {
        return new CustomUserDetailsService(userDao);
    }
}
