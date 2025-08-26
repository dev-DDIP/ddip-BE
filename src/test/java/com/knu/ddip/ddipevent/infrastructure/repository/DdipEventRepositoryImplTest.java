package com.knu.ddip.ddipevent.infrastructure.repository;

import com.knu.ddip.ddipevent.domain.DdipEvent;
import com.knu.ddip.ddipevent.infrastructure.DdipMapper;
import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DdipEventRepositoryImplTest {

    @InjectMocks
    private DdipEventRepositoryImpl ddipEventRepository;

    @Mock
    private DdipEventJpaRepository ddipEventJpaRepository;

    @Mock
    private DdipMapper ddipMapper;

    @DisplayName("띱 이벤트 저장 성공")
    @Test
    void givenDdipEvent_whenSave_thenDdipEventIsReturned() {
        // given
        DdipEvent ddipEvent = DdipEvent.builder().build();
        DdipEventEntity ddipEventEntity = DdipEventEntity.builder().build();

        given(ddipMapper.toEntity(any(DdipEvent.class))).willReturn(ddipEventEntity);
        given(ddipEventJpaRepository.save(any(DdipEventEntity.class))).willReturn(ddipEventEntity);
        given(ddipMapper.toDomain(any(DdipEventEntity.class))).willReturn(ddipEvent);

        // when
        DdipEvent savedDdipEvent = ddipEventRepository.save(ddipEvent);

        // then
        assertThat(savedDdipEvent).isEqualTo(ddipEvent);
        verify(ddipMapper).toEntity(ddipEvent);
        verify(ddipEventJpaRepository).save(ddipEventEntity);
        verify(ddipMapper).toDomain(ddipEventEntity);
    }

    @DisplayName("ID로 띱 이벤트 조회 성공")
    @Test
    void givenId_whenFindById_thenOptionalOfDdipEventIsReturned() {
        // given
        UUID id = UUID.randomUUID();
        DdipEvent ddipEvent = DdipEvent.builder().id(id).build();
        DdipEventEntity ddipEventEntity = DdipEventEntity.builder().id(id).build();

        given(ddipEventJpaRepository.findById(id)).willReturn(Optional.of(ddipEventEntity));
        given(ddipMapper.toDomain(ddipEventEntity)).willReturn(ddipEvent);

        // when
        Optional<DdipEvent> foundDdipEvent = ddipEventRepository.findById(id);

        // then
        assertThat(foundDdipEvent).isPresent();
        assertThat(foundDdipEvent.get().getId()).isEqualTo(id);
        verify(ddipEventJpaRepository).findById(id);
        verify(ddipMapper).toDomain(ddipEventEntity);
    }
}
