package com.knu.ddip.ddipevent.infrastructure;

import com.knu.ddip.ddipevent.domain.DdipEvent;
import com.knu.ddip.ddipevent.domain.DdipStatus;
import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DdipMapperTest {

    private final DdipMapper ddipMapper = new DdipMapper();

    @DisplayName("도메인을 엔티티로 변환 성공")
    @Test
    void givenDdipEventDomain_whenToEntity_thenDdipEventEntityIsReturned() {
        // given
        DdipEvent domain = DdipEvent.builder()
                .id(UUID.randomUUID())
                .title("title")
                .content("content")
                .requesterId(UUID.randomUUID())
                .reward(1000)
                .latitude(35.888)
                .longitude(128.61)
                .createdAt(Instant.now())
                .status(DdipStatus.OPEN)
                .selectedResponderId(UUID.randomUUID())
                .applicants(new ArrayList<>()) 
                .photos(new ArrayList<>()) 
                .interactions(new ArrayList<>()) 
                .difficulty(3)
                .build();

        // when
        DdipEventEntity entity = ddipMapper.toEntity(domain);

        // then
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getTitle()).isEqualTo(domain.getTitle());
        assertThat(entity.getContent()).isEqualTo(domain.getContent());
        assertThat(entity.getRequesterId()).isEqualTo(domain.getRequesterId());
        assertThat(entity.getReward()).isEqualTo(domain.getReward());
        assertThat(entity.getLatitude()).isEqualTo(domain.getLatitude());
        assertThat(entity.getLongitude()).isEqualTo(domain.getLongitude());
        assertThat(entity.getCreatedAt()).isEqualTo(domain.getCreatedAt());
        assertThat(entity.getStatus()).isEqualTo(domain.getStatus());
        assertThat(entity.getSelectedResponderId()).isEqualTo(domain.getSelectedResponderId());
        assertThat(entity.getApplicants()).isEqualTo(domain.getApplicants());
        assertThat(entity.getDifficulty()).isEqualTo(domain.getDifficulty());
    }

    @DisplayName("엔티티를 도메인으로 변환 성공")
    @Test
    void givenDdipEventEntity_whenToDomain_thenDdipEventDomainIsReturned() {
        // given
        DdipEventEntity entity = DdipEventEntity.builder()
                .id(UUID.randomUUID())
                .title("title")
                .content("content")
                .requesterId(UUID.randomUUID())
                .reward(1000)
                .latitude(35.888)
                .longitude(128.61)
                .createdAt(Instant.now())
                .status(DdipStatus.OPEN)
                .selectedResponderId(UUID.randomUUID())
                .applicants(new ArrayList<>()) 
                .photos(new ArrayList<>()) 
                .interactions(new ArrayList<>()) 
                .difficulty(3)
                .build();

        // when
        DdipEvent domain = ddipMapper.toDomain(entity);

        // then
        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getTitle()).isEqualTo(entity.getTitle());
        assertThat(domain.getContent()).isEqualTo(entity.getContent());
        assertThat(domain.getRequesterId()).isEqualTo(entity.getRequesterId());
        assertThat(domain.getReward()).isEqualTo(entity.getReward());
        assertThat(domain.getLatitude()).isEqualTo(entity.getLatitude());
        assertThat(domain.getLongitude()).isEqualTo(entity.getLongitude());
        assertThat(domain.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
        assertThat(domain.getSelectedResponderId()).isEqualTo(entity.getSelectedResponderId());
        assertThat(domain.getApplicants()).isEqualTo(entity.getApplicants());
        assertThat(domain.getDifficulty()).isEqualTo(entity.getDifficulty());
    }
}
