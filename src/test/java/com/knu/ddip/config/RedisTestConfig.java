package com.knu.ddip.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@TestConfiguration
public class RedisTestConfig {

    @Bean
    @Primary
    public LettuceConnectionFactory testRedisConnectionFactory() {
        String host = System.getProperty("spring.data.redis.host", "localhost");
        int port = Integer.parseInt(System.getProperty("spring.data.redis.port", "6379"));
        String password = System.getProperty("spring.data.redis.password", "");

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        if (!password.isEmpty()) {
            config.setPassword(password);
        }

        return new LettuceConnectionFactory(config);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> testRedisTemplate(LettuceConnectionFactory testRedisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(testRedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public RedissonClient testRedissonClient() {
        String host = System.getProperty("spring.data.redis.host", "localhost");
        int port = Integer.parseInt(System.getProperty("spring.data.redis.port", "6379"));
        String password = System.getProperty("spring.data.redis.password", "");

        Config config = new Config();
        String address = String.format("redis://%s:%d", host, port);
        config.useSingleServer()
                .setAddress(address)
                .setPassword(password.isEmpty() ? null : password);
        return Redisson.create(config);
    }
}
