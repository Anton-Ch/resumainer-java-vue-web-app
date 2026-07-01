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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

/**
 * Non-Boot Spring Security configuration for ResumAIner.
 *
 * <p><b>Phase 7 — Authorization rules active:</b>
 * <ul>
 *   <li>Public: landing {@code GET /}, static assets, error pages, favicon, CSRF bootstrap,
 *       auth endpoints ({@code /api/auth/**}), public resume links, CORS preflight.</li>
 *   <li>Admin: {@code /api/admin/**} requires {@code ADMIN} role.</li>
 *   <li>All other requests require authentication.</li>
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
 *
 * <p>Uses explicit {@link AntPathRequestMatcher} and {@link RegexRequestMatcher}
 * instead of string-based matchers because this bean lives in the root context
 * (not the servlet context), and MVC request matchers require {@code HandlerMappingIntrospector}
 * from the servlet context. Explicit matchers avoid this cross-context dependency.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /** Regex that matches public resume URLs: /{username}/{publicCode}. */
    static final String PUBLIC_RESUME_REGEX = "^/(?!api/|app/|static/|assets/|error/)[A-Za-z0-9._-]+/[A-Za-z0-9]+$";

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Phase 7 — Security filter chain with full authorization rules.
     *
     * <p>Uses explicit {@link AntPathRequestMatcher} and {@link RegexRequestMatcher}
     * instead of string-based {@code MvcRequestMatcher} because this config
     * lives in the root context (see class-level docs).
     *
     * <p>CSRF via CookieCsrfTokenRepository with SpaCsrfTokenRequestHandler.
     * Auth and public endpoints excluded from CSRF.
     *
     * <p>Security headers are provided by {@code WebConfig.securityHeadersFilter}.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authenticationManager,
                                           UserDao userDao) throws Exception {
        log.info("Phase 7 — Configuring Spring Security with full authorization rules");

        http
            // Phase 7 — Authorization rules using explicit request matchers.
            //
            // SecurityConfig is loaded in the root context. Using explicit
            // AntPathRequestMatcher/RegexRequestMatcher instead of string patterns
            // avoids MvcRequestMatcher dependency on HandlerMappingIntrospector
            // which lives in the servlet context.
            //
            // Order matters: more specific rules first, catch-all last.
            .authorizeHttpRequests(auth -> auth
                // CORS preflight — must be open for all origins
                .requestMatchers(new AntPathRequestMatcher("/**", "OPTIONS")).permitAll()
                // Landing page and static assets
                .requestMatchers(new AntPathRequestMatcher("/", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/static/**", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/error/**", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/favicon.ico", "GET")).permitAll()
                // Public resume links: /{username}/{publicCode}
                .requestMatchers(new RegexRequestMatcher(PUBLIC_RESUME_REGEX, "GET")).permitAll()
                // Auth flows, CSRF bootstrap, public API
                .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/csrf")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/csrf/ping")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/public/**")).permitAll()
                // Admin API — ADMIN role only
                .requestMatchers(new AntPathRequestMatcher("/api/admin/**")).hasRole("ADMIN")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            // Phase 6: Spring Security CSRF for SPA with CookieCsrfTokenRepository
            // Cookie XSRF-TOKEN (non-HTTP-only so JS can read it), header X-XSRF-TOKEN
            // Uses SpaCsrfTokenRequestHandler for BREACH-safe rendering and SPA-compatible
            // token resolution: raw cookie token is matched against X-XSRF-TOKEN header.
            // Deferred token is forced to load on every response via handle().
            // Public endpoints excluded using AntPathRequestMatcher.
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new com.resumainer.service.security.SpaCsrfTokenRequestHandler())
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/api/auth/**"),
                    new AntPathRequestMatcher("/api/public/**")
                )
            )
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
        log.info("Phase 6 — Spring Security CSRF enabled via CookieCsrfTokenRepository");
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
