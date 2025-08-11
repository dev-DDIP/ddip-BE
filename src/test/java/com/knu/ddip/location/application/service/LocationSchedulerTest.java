package com.knu.ddip.location.application.service;

import com.knu.ddip.config.IntegrationTestConfig;
import com.knu.ddip.config.MySQLTestContainerConfig;
import com.knu.ddip.config.RedisTestContainerConfig;
import com.knu.ddip.config.TestEnvironmentConfig;
import com.knu.ddip.location.application.scheduler.LocationScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith({RedisTestContainerConfig.class, MySQLTestContainerConfig.class, TestEnvironmentConfig.class})
@Import(IntegrationTestConfig.class)
class LocationSchedulerTest {

    @Autowired
    LocationScheduler locationScheduler;
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void cleanupExpiredUserLocationsTest() {
        // given
        String cellId = "cellId";
        String cellUsersKey = "cell:" + cellId + ":users";
        String cellExpiryKey = "cell:" + cellId + ":expiry";

        long now = System.currentTimeMillis();

        // 만료된 위치
        String userId = "userId";
        long expireAt = now - (2 * 3600 * 1000L);
        redisTemplate.opsForSet().add(cellUsersKey, userId);
        redisTemplate.opsForZSet().add(cellExpiryKey, userId, (double) expireAt);

        // 만료되지 않은 위치
        String userId2 = "userId2";
        long expireAt2 = now + 3600 * 1000L;
        redisTemplate.opsForSet().add(cellUsersKey, userId2);
        redisTemplate.opsForZSet().add(cellExpiryKey, userId2, (double) expireAt2);

        // when
        locationScheduler.cleanupExpiredUserLocations();

        // then
        assertThat(redisTemplate.opsForSet().isMember(cellUsersKey, userId)).isFalse();
        assertThat(redisTemplate.opsForZSet().score(cellExpiryKey, userId)).isNull();
    }

}