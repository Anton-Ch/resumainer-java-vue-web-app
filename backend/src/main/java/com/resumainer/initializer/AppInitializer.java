package com.resumainer.initializer;

import com.resumainer.config.WebConfig;
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
 * <p>
 * No root config is needed for this feature — all Spring configuration
 * is contained in {@link WebConfig}.
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
        // Throw NoHandlerFoundException for unhandled URLs so GlobalExceptionHandler
        // can serve our custom bilingual 404 Thymeleaf template instead of Tomcat default.
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
        return dispatcherServlet;
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        // Enable case-sensitive REST-style mapping (default is true, explicit for clarity)
        registration.setInitParameter("spring.mvc.static-path-pattern", "/static/**");
    }
}
