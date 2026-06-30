package com.resumainer.initializer;

import com.resumainer.config.RootConfig;
import com.resumainer.config.WebConfig;
import com.resumainer.security.SecurityConfig;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Servlet container initializer for Spring MVC.
 * <p>
 * Bootstraps the DispatcherServlet without web.xml by extending
 * {@link AbstractAnnotationConfigDispatcherServletInitializer}.
 * Auto-discovered by Tomcat 10.1+ via the ServletContainerInitializer SPI.
 * <p>
 * <b>Phase 6 change:</b> Legacy {@code CsrfFilter} removed — Spring Security CSRF
 * via {@code CookieCsrfTokenRepository} handles CSRF protection now.
 */
public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{RootConfig.class, SecurityConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected DispatcherServlet createDispatcherServlet(WebApplicationContext servletAppContext) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet(servletAppContext);
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
        return dispatcherServlet;
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setInitParameter("spring.mvc.static-path-pattern", "/static/**");

        // Session timeout: 90 minutes (5400 seconds)
        // Increased from 30m to 90m for profile editing UX (SEC-011):
        // users fill multi-section forms without autosave — 30m risks data loss on save.
        // 90m is safe for resume/profile apps: session is bound to HttpSession cookie
        // with Secure+HttpOnly+SameSite attributes, CSRF protected, and timed out server-side.
        registration.setInitParameter("spring.mvc.servlet.session-timeout", "5400");
    }

    /**
     * Register servlet filters for all requests.
     * <p>
     * Phase 6: Only the Spring Security filter chain is registered via
     * {@link DelegatingFilterProxy}. Legacy {@code CsrfFilter} has been removed —
     * Spring Security CSRF with {@code CookieCsrfTokenRepository} handles CSRF.
     * <p>
     * In pure Spring MVC, use getServletFilters() — NOT FilterRegistrationBean (see B6).
     */
    @Override
    protected Filter[] getServletFilters() {
        return new Filter[]{
                new DelegatingFilterProxy("springSecurityFilterChain")
        };
    }
}
