package com.knu.ddip.common.config;

import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@RequiredArgsConstructor
@Configuration
public class RedissonConfig {

    private final Environment env;

    @Bean
    public RedissonClient redissonClient() {
        String host = env.getProperty("spring.data.redis.host", "localhost");
        int port = env.getProperty("spring.data.redis.port", Integer.class, 6379);
        String password = env.getProperty("spring.data.redis.password", "");

        Config config = new Config();

        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setPassword(password)
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(1);

        return Redisson.create(config);
    }
}
