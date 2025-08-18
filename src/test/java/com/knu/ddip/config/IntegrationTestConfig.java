package com.knu.ddip.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;


@TestConfiguration
@Import({
        RedisTestConfig.class,
        MySQLTestConfig.class
})
public class IntegrationTestConfig {
}
