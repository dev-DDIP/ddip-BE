package com.knu.ddip.config;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class MySQLTestContainerConfig implements BeforeAllCallback {
    private static final String MYSQL_IMAGE = "mysql:8.0.36";
    private static final String MYSQL_DB = "test_db";
    private static final String MYSQL_USER = "test_user";
    private static final String MYSQL_PASSWORD = "test_password";

    private static final int MYSQL_PORT = 3306;
    private static GenericContainer<?> mysqlContainer;

    @Override
    public void beforeAll(ExtensionContext context) {
        mysqlContainer = new GenericContainer<>(DockerImageName.parse(MYSQL_IMAGE))
                .withExposedPorts(MYSQL_PORT)
                .withEnv("MYSQL_DATABASE", MYSQL_DB)
                .withEnv("MYSQL_USER", MYSQL_USER)
                .withEnv("MYSQL_PASSWORD", MYSQL_PASSWORD)
                .withEnv("MYSQL_ROOT_PASSWORD", "root");

        mysqlContainer.start();

        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                mysqlContainer.getHost(),
                mysqlContainer.getMappedPort(MYSQL_PORT),
                MYSQL_DB
        );

        System.setProperty("spring.datasource.url", jdbcUrl);
        System.setProperty("spring.datasource.username", MYSQL_USER);
        System.setProperty("spring.datasource.password", MYSQL_PASSWORD);
        System.setProperty("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");

        System.setProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        System.setProperty("spring.jpa.hibernate.ddl-auto", "create-drop");
        System.setProperty("SPRING_JPA_SHOW_SQL", "false");
    }
}
