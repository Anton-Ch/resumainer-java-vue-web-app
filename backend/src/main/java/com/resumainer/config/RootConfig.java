package com.resumainer.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Root {@code WebApplicationContext} configuration.
 *
 * <p>Loaded by {@code AppInitializer.getRootConfigClasses()}. Scans only
 * infrastructure (connection pool, DataSource) and DAO packages.
 * These beans are visible to both the root and servlet contexts.
 *
 * <p>The servlet context (via {@link WebConfig}) excludes these packages
 * to prevent duplicate bean creation.
 */
@Configuration
@ComponentScan(basePackages = {
        "com.resumainer.infrastructure.db",
        "com.resumainer.dao"
})
public class RootConfig {
}
