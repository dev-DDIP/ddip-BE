package com.knu.ddip.location.infrastructure.repositoroy;

import com.knu.ddip.config.IntegrationTestConfig;
import com.knu.ddip.config.MySQLTestContainerConfig;
import com.knu.ddip.config.RedisTestContainerConfig;
import com.knu.ddip.config.TestEnvironmentConfig;
import com.knu.ddip.location.exception.LocationNotFoundException;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Transactional
@SpringBootTest
@ExtendWith({RedisTestContainerConfig.class, MySQLTestContainerConfig.class, TestEnvironmentConfig.class})
@Import(IntegrationTestConfig.class)
class LocationRepositoryImplTest {

    @Autowired
    LocationRepositoryImpl locationRepository;
    @Autowired
    LocationJpaRepository locationJpaRepository;
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    private LocationRepositoryImpl locationRepositoryImpl;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void saveAllTest() {
        // given
        List<String> cellIds = List.of("a", "b", "c");

        // when
        locationRepository.saveAll(cellIds);

        List<LocationEntity> locations = locationJpaRepository.findAll();
        List<String> findCellIds = locations.stream()
                .map(LocationEntity::getCellId)
                .collect(Collectors.toList());

        // then
        assertThat(findCellIds).containsAll(cellIds);
    }

    @Test
    void saveUserIdByCellIdTest() {
        // given
        String userId = "saveUserIdByCellIdTest";
        String cellId = "saveUserIdByCellIdTest";

        String cellIdUsersKey = createCellIdUsersKey(cellId);
        String cellIdExpiriesKey = createCellIdExpiriesKey(cellId);

        // when
        locationRepositoryImpl.saveUserIdByCellId(userId, cellId);

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
    void validateLocationByValidCellIdTest() {
        // given
        String validCellId = "validCellId";
        locationJpaRepository.save(LocationEntity.create(validCellId));

        // when // then
        assertDoesNotThrow(() -> locationRepositoryImpl.validateLocationByCellId(validCellId));
    }

    @Test
    void validateLocationByInvalidCellIdTest() {
        // given
        String validCellId = "invalidCellId";

        // when // then
        assertThatThrownBy(() -> locationRepositoryImpl.validateLocationByCellId(validCellId))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("위치를 찾을 수 없습니다.");
    }

    @Test
    void findAllLocationsByCellIdInTest() {
        // given
        List<String> cellIds = List.of(
                "findAllLocationsByCellIdInTest1",
                "findAllLocationsByCellIdInTest2"
        );
        List<LocationEntity> locations = cellIds.stream()
                .map(LocationEntity::create)
                .collect(Collectors.toList());
        locationJpaRepository.saveAll(locations);

        // when
        List<String> findCellIds = locationRepositoryImpl.findAllLocationsByCellIdIn(cellIds);

        // then
        assertThat(findCellIds).hasSize(2)
                .containsAll(cellIds);
    }

    @Test
    void findUserIdsByCellIdsTest() {
        // 여 테스트 작성
        // given
        List<String> cellIds = List.of(
                "findUserIdsByCellIdsTest1",
                "findUserIdsByCellIdsTest2"
        );
        List<LocationEntity> locations = cellIds.stream()
                .map(LocationEntity::create)
                .collect(Collectors.toList());
        locationJpaRepository.saveAll(locations);

        List<String> userIds = List.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        for (int i = 0; i < 2; i++) {
            locationRepositoryImpl.saveUserIdByCellId(userIds.get(i), cellIds.get(i));
        }
        // 포함되지 않는 셀, 유저 데이터
        locationRepositoryImpl.saveUserIdByCellId(UUID.randomUUID().toString(), "notIncludedCellId");

        // when
        List<String> findCellIds = locationRepositoryImpl.findUserIdsByCellIds(cellIds);

        // then
        assertThat(findCellIds).hasSize(2)
                .containsAll(userIds);
    }

    private String createUserIdKey(String encodedUserId) {
        return "user:" + encodedUserId;
    }

    private String createCellIdUsersKey(String cellId) {
        return "cell:" + cellId + ":users";
    }

    private String createCellIdExpiriesKey(String cellId) {
        return "cell:" + cellId + ":expiry";
    }

}