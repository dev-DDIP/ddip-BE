package com.knu.ddip.location.infrastructure.repository;

import com.knu.ddip.config.IntegrationTestConfig;
import com.knu.ddip.config.MySQLTestContainerConfig;
import com.knu.ddip.config.RedisTestContainerConfig;
import com.knu.ddip.config.TestEnvironmentConfig;
import com.knu.ddip.location.infrastructure.entity.LocationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.knu.ddip.location.application.util.LocationKeyFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@ExtendWith({RedisTestContainerConfig.class, MySQLTestContainerConfig.class, TestEnvironmentConfig.class})
@Import(IntegrationTestConfig.class)
class LocationWriterImplTest {

    @Autowired
    LocationReaderImpl locationReader;
    @Autowired
    LocationWriterImpl locationWriter;
    @Autowired
    LocationJpaRepository locationJpaRepository;
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void saveAllTest() {
        // given
        List<String> cellIds = List.of("a", "b", "c");

        // when
        locationWriter.saveAll(cellIds);

        List<LocationEntity> locations = locationJpaRepository.findAll();
        List<String> findCellIds = locations.stream()
                .map(LocationEntity::getCellId)
                .collect(Collectors.toList());

        // then
        assertThat(findCellIds).containsAll(cellIds);
    }

    @Test
    void saveUserIdByCellIdAtomicTest() {
        // given
        String userId = "saveUserIdByCellIdTest";
        String cellId = "saveUserIdByCellIdTest";

        String cellIdUsersKey = createCellIdUsersKey(cellId);
        String cellIdExpiriesKey = createCellIdExpiriesKey(cellId);

        // when
        locationWriter.saveUserIdByCellIdAtomic(cellId, false, userId);

        // then
        assertThat(redisTemplate.opsForSet().isMember(cellIdUsersKey, cellId)).isTrue();
        assertThat(redisTemplate.opsForZSet().score(cellIdExpiriesKey, cellId)).isNotNull();
    }

    @Test
    void saveCellIdByUserIdTest() {
        // given
        String userId = "saveCellIdByUserIdTest";
        String cellId = "saveCellIdByUserIdTest";
        String userIdKey = createUserIdKey(userId);

        // when
        redisTemplate.opsForSet().add(userIdKey, cellId);

        // then
        assertThat(redisTemplate.opsForSet().isMember(userIdKey, cellId)).isTrue();
    }

    @Test
    void cleanupExpiredUserLocationsTest() {
        // given
        long now = System.currentTimeMillis();

        String cellId = "cellId";
        String cellUsersKey = "cell:" + cellId + ":users";
        String cellExpiryKey = "cell:" + cellId + ":expiry";

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
        locationWriter.cleanupExpiredUserLocations(now);

        // then
        assertThat(redisTemplate.opsForSet().isMember(cellUsersKey, userId)).isFalse();
        assertThat(redisTemplate.opsForZSet().score(cellExpiryKey, userId)).isNull();
    }
}