package com.resumainer.initializer;

import com.resumainer.config.WebConfig;
import com.resumainer.filter.CsrfFilter;
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
 * <b>Phase 1 change:</b> Root config now loads {@link SecurityConfig} to create a
 * root {@code WebApplicationContext} where Spring Security's
 * {@code springSecurityFilterChain} bean is defined.
 * The filter is registered via {@link DelegatingFilterProxy} in {@link #getServletFilters()}.
 */
public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{SecurityConfig.class};
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
     * Order:
     * <ol>
     *   <li>{@link DelegatingFilterProxy} for {@code springSecurityFilterChain}
     *       (Spring Security filter chain in permissive Phase 1 mode)</li>
     *   <li>{@link CsrfFilter} (legacy custom CSRF — stays active until Phase 6)</li>
     * </ol>
     * <p>
     * In pure Spring MVC, use getServletFilters() — NOT FilterRegistrationBean (see B6).
     * The root {@code WebApplicationContext} (loaded from {@link SecurityConfig}) provides
     * the {@code springSecurityFilterChain} bean that {@link DelegatingFilterProxy} resolves.
     */
    @Override
    protected Filter[] getServletFilters() {
        return new Filter[]{
                new DelegatingFilterProxy("springSecurityFilterChain"),
                new CsrfFilter()
        };
    }
}
