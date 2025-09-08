package com.knu.ddip.ddipevent.infrastructure.repository;

import com.knu.ddip.config.IntegrationTestConfig;
import com.knu.ddip.config.MySQLTestContainerConfig;
import com.knu.ddip.config.RedisTestContainerConfig;
import com.knu.ddip.config.TestEnvironmentConfig;
import com.knu.ddip.ddipevent.domain.DdipEvent;
import com.knu.ddip.location.application.util.S2Converter;
import com.knu.ddip.user.infrastructure.repository.UserRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
        List<DdipEvent> ddipEvents = List.of(
                DdipEvent.create("대운동장", "대운동장", 1, 35.8880523, 128.6058911, 1, UUID.randomUUID()),
                DdipEvent.create("공대9호관", "공대9호관", 1, 35.8868876, 128.6082622, 1, UUID.randomUUID()),
                DdipEvent.create("융복합관", "융복합관", 1, 35.8880089, 128.6114594, 1, UUID.randomUUID())
        );
        for (DdipEvent ddipEvent : ddipEvents) {
            ddipEventRepositoryImpl.save(ddipEvent);
        }

        // when
        List<DdipEvent> sortDdipEvents = ddipEventRepositoryImpl.findWithinBounds(35.8853838, 128.6058911, 35.8955185, 128.6140665, "sort", 35.8886499, 128.6121487); // 일청담

        // then
        assertThat(sortDdipEvents).hasSize(3)
                .extracting(DdipEvent::getContent)
                .containsExactly("융복합관", "공대9호관", "대운동장"); // 거리 가까운 순 정렬
    }

}