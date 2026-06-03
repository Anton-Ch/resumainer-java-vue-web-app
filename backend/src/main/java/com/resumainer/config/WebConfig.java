package com.resumainer.config;

import com.resumainer.controller.AuthController;
import com.resumainer.controller.LandingPageController;
import com.resumainer.dao.ContactDetailDao;
import com.resumainer.dao.LanguageDao;
import com.resumainer.dao.RoleDao;
import com.resumainer.dao.UserDao;
import com.resumainer.dao.UserPermissionDao;
import com.resumainer.dao.UserStatusDao;
import com.resumainer.exception.GlobalExceptionHandler;
import com.resumainer.interceptor.AuthInterceptor;
import com.resumainer.service.AuthService;
import com.resumainer.service.PasswordService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Locale;

/**
 * Spring MVC configuration for the ResumAIner backend.
 * <p>
 * Configures Thymeleaf template engine, internationalization (i18n) with
 * EN/RU support, browser locale detection, locale switching via {@code ?lang=},
 * and security response headers.
 * <p>
 * All beans are registered explicitly for predictable and auditable
 * configuration — no component scanning.
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    private final ApplicationContext applicationContext;

    public WebConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // ============================================================
    // Thymeleaf Template Engine
    // ============================================================

    /**
     * Resolves Thymeleaf templates from {@code /templates/} directory
     * on the classpath with {@code .html} suffix.
     */
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(true);
        return resolver;
    }

    /**
     * Spring-integrated Thymeleaf engine. Automatically applies the
     * SpringStandardDialect and enables Spring's MessageSource for
     * message resolution in templates.
     */
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        return engine;
    }

    /**
     * View resolver that maps logical view names to Thymeleaf templates.
     */
    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(1);
        return resolver;
    }

    // ============================================================
    // Internationalization (i18n)
    // ============================================================

    /**
     * Message source loading {@code messages.properties} and
     * {@code messages_*.properties} from the classpath root.
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }

    /**
     * Locale resolver that stores the user's language choice in a cookie.
     * <p>
     * Default locale is English. On first visit, resolves from the browser's
     * {@code Accept-Language} header. Russian (ru) is detected automatically;
     * any other language falls back to English.
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setCookieName("resumainer-lang");
        resolver.setCookieMaxAge(60 * 60 * 24 * 365); // 1 year
        resolver.setCookieHttpOnly(true);
        return resolver;
    }

    /**
     * Interceptor that allows language switching via {@code ?lang=en} or
     * {@code ?lang=ru}. Invalid locale values are silently ignored,
     * keeping the current locale.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        interceptor.setIgnoreInvalidLocale(true);
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        // AuthInterceptor checks for valid session on all paths except /api/auth/*
        registry.addInterceptor(authInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }

    // ============================================================
    // Static Resources
    // ============================================================

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static/");
    }

    // ============================================================
    // Database — DataSource (reads from application.properties via @Value)
    // ============================================================

    /**
     * Development DataSource using DriverManagerDataSource.
     * <p>
     * In production, replace with a proper connection pool implementation.
     * Config values come from application.properties (db.url, db.user, etc.).
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl("jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:resumainer}");
        ds.setUsername("resumainer");
        ds.setPassword("resumainer_dev");
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }

    // ============================================================
    // DAO Beans (explicit registration — no component scan)
    // ============================================================

    @Bean
    public UserDao userDao() {
        return new UserDao(dataSource());
    }

    @Bean
    public RoleDao roleDao() {
        return new RoleDao(dataSource());
    }

    @Bean
    public UserStatusDao userStatusDao() {
        return new UserStatusDao(dataSource());
    }

    @Bean
    public UserPermissionDao userPermissionDao() {
        return new UserPermissionDao(dataSource());
    }

    @Bean
    public LanguageDao languageDao() {
        return new LanguageDao(dataSource());
    }

    @Bean
    public ContactDetailDao contactDetailDao() {
        return new ContactDetailDao(dataSource());
    }

    // ============================================================
    // Service Beans
    // ============================================================

    @Bean
    public PasswordService passwordService() {
        return new PasswordService();
    }

    @Bean
    public AuthService authService() {
        return new AuthService(userDao(), roleDao(), contactDetailDao(),
                passwordService(), dataSource());
    }

    // ============================================================
    // Interceptor Beans
    // ============================================================

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    // ============================================================
    // Controller Beans (explicit registration — no component scan)
    // ============================================================

    @Bean
    public AuthController authController() {
        return new AuthController(authService());
    }

    /**
     * Landing page controller serving the root URL.
     * <p>
     * Registered explicitly as a {@link Bean} because in pure Spring MVC
     * (without Boot), {@code @Controller} alone does NOT register the
     * controller as a Spring bean (see BUGS.md B1).
     */
    @Bean
    public LandingPageController landingPageController() {
        return new LandingPageController();
    }

    /**
     * Global exception handler for 404/500 with bilingual Thymeleaf templates.
     * <p>
     * Registered explicitly as a {@link Bean} because in pure Spring MVC
     * (without Boot), {@code @ControllerAdvice} alone does NOT register the
     * handler (same pattern as BUGS.md B1 for {@code @Controller}).
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    // ============================================================
    // Security Headers Filter
    // ============================================================

    /**
     * Filter that sets security response headers on every response:
     * <ul>
     *   <li>{@code X-Content-Type-Options: nosniff}</li>
     *   <li>{@code X-Frame-Options: DENY}</li>
     *   <li>{@code Referrer-Policy: same-origin}</li>
     *   <li>{@code Content-Security-Policy} restricting to self-hosted assets</li>
     * </ul>
     */
    @Bean
    public Filter securityHeadersFilter() {
        return new Filter() {
            @Override
            public void init(FilterConfig filterConfig) {
                // No initialization needed
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response,
                                 FilterChain chain) throws IOException, ServletException {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setHeader("X-Content-Type-Options", "nosniff");
                httpResponse.setHeader("X-Frame-Options", "DENY");
                httpResponse.setHeader("Referrer-Policy", "same-origin");
                httpResponse.setHeader("Content-Security-Policy",
                        "default-src 'self'; style-src 'self' 'unsafe-inline'; "
                        + "font-src 'self'; img-src 'self' data:; script-src 'self'");
                chain.doFilter(request, response);
            }

            @Override
            public void destroy() {
                // No cleanup needed
            }
        };
    }
}
