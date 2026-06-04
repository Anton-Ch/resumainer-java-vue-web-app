package com.resumainer.infrastructure.db;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

/**
 * Spring configuration for the custom JDBC connection pool.
 * <p>
 * Exposes the pool as a {@link DataSource} bean with init/destroy lifecycle.
 * After Capstone acceptance, replace this with:
 * <pre>{@code
 * @Bean(destroyMethod = "close")
 * public DataSource dataSource() {
 *     HikariConfig config = new HikariConfig();
 *     config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + dbName);
 *     config.setUsername(user);
 *     config.setPassword(password);
 *     config.setMaximumPoolSize(10);
 *     return new HikariDataSource(config);
 * }
 * }</pre>
 */
@Configuration
public class DataSourceConfig {

    @Bean(initMethod = "init", destroyMethod = "close")
    public DataSource dataSource() {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "5432");
        String dbName = System.getenv().getOrDefault("DB_NAME", "resumainer");
        String user = System.getenv().getOrDefault("DB_USER", "resumainer");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "resumainer_dev");

        ConnectionPoolConfig config = new ConnectionPoolConfig(
                "jdbc:postgresql://" + host + ":" + port + "/" + dbName,
                user, password, 2, 10, 5000, 2
        );
        ConnectionFactory factory = new ConnectionFactory(config);
        return new SimpleConnectionPool(config, factory);
    }
}
