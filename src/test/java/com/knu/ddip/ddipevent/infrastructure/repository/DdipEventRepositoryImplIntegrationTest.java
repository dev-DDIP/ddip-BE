package com.knu.ddip.ddipevent.infrastructure.repository;

import com.knu.ddip.config.IntegrationTestConfig;
import com.knu.ddip.config.MySQLTestContainerConfig;
import com.knu.ddip.config.RedisTestContainerConfig;
import com.knu.ddip.config.TestEnvironmentConfig;
import com.knu.ddip.ddipevent.domain.DdipEvent;
import com.knu.ddip.ddipevent.domain.DdipStatus;
import com.knu.ddip.ddipevent.fixture.DdipEventFixture;
import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;
import com.knu.ddip.location.application.util.S2Converter;
import com.knu.ddip.user.infrastructure.repository.UserRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ExtendWith({RedisTestContainerConfig.class, MySQLTestContainerConfig.class, TestEnvironmentConfig.class})
@Import({IntegrationTestConfig.class, UserRepositoryImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DdipEventRepositoryImplIntegrationTest {

    @Autowired
    DdipEventJpaRepository ddipEventJpaRepository;
    @Autowired
    DdipEventRepositoryImpl ddipEventRepositoryImpl;
    @Autowired
    S2Converter s2Converter;

    @Test
    void findWithinBoundsTest() {
        // given
        List<DdipEventEntity> ddipEvents = List.of(
                DdipEventFixture.createDdipEvent(35.8880523, 128.6058911, DdipStatus.OPEN, "대운동장", s2Converter.toCellIdString(35.8880523, 128.6058911)), // 대운동장
                DdipEventFixture.createDdipEvent(35.8868876, 128.6082622, DdipStatus.OPEN, "공대9호관", s2Converter.toCellIdString(35.8868876, 128.6082622)), // 공대9호관
                DdipEventFixture.createDdipEvent(35.8880089, 128.6114594, DdipStatus.OPEN, "융복합관", s2Converter.toCellIdString(35.8880089, 128.6114594)) // 융복합관
        );
        ddipEventJpaRepository.saveAll(ddipEvents);

        // when
        List<DdipEvent> sortDdipEvents = ddipEventRepositoryImpl.findWithinBounds(35.8853838, 128.6058911, 35.8955185, 128.6140665, "sort", 35.8886499, 128.6121487);// 일청담

        // then
        assertThat(sortDdipEvents).hasSize(3)
                .extracting(DdipEvent::getContent)
                .containsExactly("융복합관", "공대9호관", "대운동장"); // 거리 가까운 순 정렬
    }

    @Test
    void findWithinBoundsWithOnlyOpenEventsTest() {
        // given
        List<DdipEventEntity> ddipEvents = List.of(
                DdipEventFixture.createDdipEvent(35.8880523, 128.6058911, DdipStatus.OPEN, "대운동장", s2Converter.toCellIdString(35.8880523, 128.6058911)), // 대운동장
                DdipEventFixture.createDdipEvent(35.8868876, 128.6082622, DdipStatus.COMPLETED, "공대9호관", s2Converter.toCellIdString(35.8868876, 128.6082622)), // 공대9호관
                DdipEventFixture.createDdipEvent(35.8880089, 128.6114594, DdipStatus.COMPLETED, "융복합관", s2Converter.toCellIdString(35.8880089, 128.6114594)) // 융복합관
        );
        ddipEventJpaRepository.saveAll(ddipEvents);

        // when
        List<DdipEvent> sortDdipEvents = ddipEventRepositoryImpl.findWithinBounds(35.8853838, 128.6058911, 35.8955185, 128.6140665, "sort", 35.8886499, 128.6121487); // 일청담

        // then
        assertThat(sortDdipEvents).hasSize(1)
                .extracting(DdipEvent::getContent)
                .containsExactly("대운동장");
    }

}