package com.resumainer.config;

import com.resumainer.controller.HelloWorldController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration for the ResumAIner backend.
 * <p>
 * Enables annotation-driven MVC via {@link EnableWebMvc} and registers a
 * JSP view resolver. JSP files are placed under {@code /WEB-INF/views/}
 * to prevent direct client access.
 * <p>
 * All beans are registered explicitly for predictable and auditable
 * configuration — no component scanning.
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.jsp("/WEB-INF/views/", ".jsp");
    }

    @Bean
    public HelloWorldController helloWorldController() {
        return new HelloWorldController();
    }
}
