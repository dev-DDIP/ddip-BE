package com.knu.ddip.ddipevent.infrastructure.repository;

import com.knu.ddip.config.IntegrationTestConfig;
import com.knu.ddip.config.MySQLTestContainerConfig;
import com.knu.ddip.config.RedisTestContainerConfig;
import com.knu.ddip.config.TestEnvironmentConfig;
import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;
import com.knu.ddip.user.infrastructure.repository.UserRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static com.knu.ddip.ddipevent.fixture.DdipEventFixture.createDdipEvent;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ExtendWith({RedisTestContainerConfig.class, MySQLTestContainerConfig.class, TestEnvironmentConfig.class})
@Import({IntegrationTestConfig.class, UserRepositoryImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DdipEventJpaRepositoryTest {

    @Autowired
    DdipEventJpaRepository ddipEventJpaRepository;

    @Test
    void findAllByCellIdInTest() {
        // given
        int targetEventsNumber = 3;

        List<String> targetCellIds = List.of("TargetCellId0", "TargetCellId1", "TargetCellId2");
        String notTargetCellId = "NotTargetCellId";

        for (int i = 0; i < targetEventsNumber; i++) {
            DdipEventEntity event = createDdipEvent();
            event.setCellId(targetCellIds.get(i));
            ddipEventJpaRepository.save(event);
        }

        DdipEventEntity notTargetEvent = createDdipEvent();
        notTargetEvent.setCellId(notTargetCellId);
        ddipEventJpaRepository.save(notTargetEvent);

        // when
        List<DdipEventEntity> events = ddipEventJpaRepository.findAllByCellIdIn(targetCellIds);

        // then
        assertThat(events).hasSize(targetEventsNumber);
    }

}