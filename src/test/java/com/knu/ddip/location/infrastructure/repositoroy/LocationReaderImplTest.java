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
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Transactional
@SpringBootTest
@ExtendWith({RedisTestContainerConfig.class, MySQLTestContainerConfig.class, TestEnvironmentConfig.class})
@Import(IntegrationTestConfig.class)
class LocationReaderImplTest {

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
    void validateLocationByValidCellIdTest() {
        // given
        String validCellId = "validCellId";
        locationJpaRepository.save(LocationEntity.create(validCellId));

        // when // then
        assertDoesNotThrow(() -> locationReader.validateLocationByCellId(validCellId));
    }

    @Test
    void validateLocationByInvalidCellIdTest() {
        // given
        String validCellId = "invalidCellId";

        // when // then
        assertThatThrownBy(() -> locationReader.validateLocationByCellId(validCellId))
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
        List<String> findCellIds = locationReader.findAllLocationsByCellIdIn(cellIds);

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
            locationWriter.saveUserIdByCellIdAtomic(cellIds.get(i), false, userIds.get(i));
        }
        // 포함되지 않는 셀, 유저 데이터
        locationWriter.saveUserIdByCellIdAtomic("notIncludedCellId", true, UUID.randomUUID().toString());

        // when
        List<String> findCellIds = locationReader.findUserIdsByCellIds(cellIds);

        // then
        assertThat(findCellIds).hasSize(2)
                .containsAll(userIds);
    }

}