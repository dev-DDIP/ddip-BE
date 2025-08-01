package com.knu.ddip.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@TestConfiguration
public class MySQLTestConfig {

    @Bean
    @Primary
    public DataSource testDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getProperty("spring.datasource.url"));
        config.setUsername(System.getProperty("spring.datasource.username"));
        config.setPassword(System.getProperty("spring.datasource.password"));
        config.setDriverClassName(System.getProperty("spring.datasource.driver-class-name"));

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}
