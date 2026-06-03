package com.resumainer.initializer;

import com.resumainer.config.WebConfig;
import com.resumainer.filter.CsrfFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Servlet container initializer for Spring MVC.
 * <p>
 * Bootstraps the DispatcherServlet without web.xml by extending
 * {@link AbstractAnnotationConfigDispatcherServletInitializer}.
 * Auto-discovered by Tomcat 10.1+ via the ServletContainerInitializer SPI.
 */
public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
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

        // Session timeout: 30 minutes (1800 seconds)
        registration.setInitParameter("spring.mvc.servlet.session-timeout", "1800");
    }

    /**
     * Register servlet filters for all requests.
     * <p>
     * CsrfFilter: OWASP cookie-to-header CSRF protection.
     * In pure Spring MVC, use getServletFilters() — NOT FilterRegistrationBean (see B6).
     */
    @Override
    protected Filter[] getServletFilters() {
        return new Filter[]{
                new CsrfFilter()
        };
    }
}
