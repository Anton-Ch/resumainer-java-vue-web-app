package com.resumainer.config;

import com.resumainer.controller.AuthController;
import com.resumainer.service.AuthService;
import com.resumainer.security.SecurityConfig;
import com.resumainer.service.ResendVerificationService;
import com.resumainer.service.security.ResendVerificationRateLimiter;
import com.resumainer.service.security.TrustedProxyClientIpExtractor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.sql.DataSource;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class Phase11RealApplicationContextTest {

    @Test
    void actualRootSecurityAndWebConfigurationsStartWithSafeExternalBoundaries() {
        AnnotationConfigWebApplicationContext root = new AnnotationConfigWebApplicationContext();
        root.setServletContext(new MockServletContext());
        root.register(RootConfig.class, SecurityConfig.class);
        replaceExternalBoundary(root, "dataSource", mock(DataSource.class));

        AnnotationConfigWebApplicationContext servlet = new AnnotationConfigWebApplicationContext();
        servlet.setServletContext(root.getServletContext());
        servlet.setParent(root);
        servlet.register(WebConfig.class);
        replaceExternalBoundary(servlet, "flyway", mock(Flyway.class));

        try {
            root.refresh();
            servlet.refresh();

            AuthController controller = servlet.getBean(AuthController.class);
            assertNotNull(controller);
            assertSame(servlet.getBean(ResendVerificationService.class),
                    ReflectionTestUtils.getField(controller, "resendVerificationService"));
            assertSame(servlet.getBean(TrustedProxyClientIpExtractor.class),
                    ReflectionTestUtils.getField(controller, "clientIpExtractor"));
            assertNotNull(servlet.getBean(ResendVerificationRateLimiter.class));
            assertNotNull(servlet.getBean(TrustedProxyClientIpExtractor.class));
            assertEquals(1, BeanFactoryUtils.beansOfTypeIncludingAncestors(servlet, DataSource.class).size());
            assertEquals(1, BeanFactoryUtils.beansOfTypeIncludingAncestors(servlet, Clock.class).size(),
                    "The real parent/child graph must expose exactly one Clock bean");
            assertEquals(1, BeanFactoryUtils.beansOfTypeIncludingAncestors(servlet, AuthService.class).size());
            assertEquals(1, BeanFactoryUtils.beansOfTypeIncludingAncestors(
                    servlet, ResendVerificationService.class).size());
            assertTrue(root.containsLocalBean("rootConfig"));
            assertFalse(servlet.containsLocalBean("rootConfig"));
            assertTrue(root.containsLocalBean("securityConfig"));
            assertFalse(servlet.containsLocalBean("securityConfig"));
            assertFalse(root.containsLocalBean("webConfig"));
            assertTrue(servlet.containsLocalBean("webConfig"));
            assertFalse(servlet.containsLocalBean("phase11ContextWiringTest.TestConfig"),
                    "Nested test @Configuration must not be discovered by WebConfig");
        } finally {
            servlet.close();
            root.close();
        }
    }

    private static void replaceExternalBoundary(AnnotationConfigWebApplicationContext context,
                                                String beanName, Object replacement) {
        context.addBeanFactoryPostProcessor(beanFactory -> {
            DefaultListableBeanFactory registry = (DefaultListableBeanFactory) beanFactory;
            if (registry.containsBeanDefinition(beanName)) {
                registry.removeBeanDefinition(beanName);
            }
            registry.registerSingleton(beanName, replacement);
        });
    }
}
