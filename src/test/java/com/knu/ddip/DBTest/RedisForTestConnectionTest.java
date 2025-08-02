package com.knu.ddip.DBTest;

import com.knu.ddip.config.RedisTestConfig;
import com.knu.ddip.config.RedisTestContainerConfig;
import com.knu.ddip.config.TestEnvironmentConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@DataRedisTest
@ExtendWith({RedisTestContainerConfig.class, TestEnvironmentConfig.class})
@Import(RedisTestConfig.class)
class RedisForTestConnectionTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedisConnection() {
        // Given
        String testKey = "test:connection";
        String testValue = "connection-successful";

        // When
        redisTemplate.opsForValue().set(testKey, testValue);
        String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);

        // Then
        assertThat(retrievedValue).isEqualTo(testValue);

        // Cleanup
        redisTemplate.delete(testKey);
    }

    @Test
    public void testRedisConnectionInfo() {
        // When
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();

        try {
            // Then
            assertThat(connection).isNotNull();
            assertThat(connection.ping()).isNotNull();

            System.out.println("Redis connection successful");
            System.out.println("Redis ping response: " + new String(connection.ping()));
        } finally {
            connection.close();
        }
    }

    @Test
    public void testRedisOperations() {
        // Given
        String hashKey = "test:hash";
        String field = "field1";
        String value = "value1";

        // When
        redisTemplate.opsForHash().put(hashKey, field, value);
        String retrievedValue = (String) redisTemplate.opsForHash().get(hashKey, field);

        // Then
        assertThat(retrievedValue).isEqualTo(value);

        // Cleanup
        redisTemplate.delete(hashKey);
    }
}
