package com.knu.ddip.ddipevent.application.service;

import com.knu.ddip.ddipevent.application.dto.CreateDdipRequestDto;
import com.knu.ddip.ddipevent.application.dto.DdipEventDetailDto;
import com.knu.ddip.ddipevent.application.dto.DdipEventSummaryDto;
import com.knu.ddip.ddipevent.application.dto.FeedRequestDto;
import com.knu.ddip.ddipevent.domain.DdipEvent;
import com.knu.ddip.ddipevent.domain.DdipStatus;
import com.knu.ddip.ddipevent.exception.DdipNotFoundException;
import com.knu.ddip.ddipevent.application.util.DistanceConverter;
import com.knu.ddip.user.business.dto.UserEntityDto;
import com.knu.ddip.user.business.service.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DdipServiceTest {

    @InjectMocks
    private DdipService ddipService;

    @Mock
    private DdipEventRepository ddipEventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DistanceConverter distanceConverter;

    @DisplayName("띱 이벤트 생성 성공")
    @Test
    void givenCreateDdipRequest_whenCreateDdipEvent_thenDdipEventDetailDtoIsReturned() {
        // given
        CreateDdipRequestDto requestDto = new CreateDdipRequestDto("title", "content", 1000, 35.888, 128.61, 3);
        UUID requesterId = UUID.randomUUID();
        UserEntityDto userEntityDto = UserEntityDto.builder().id(requesterId).build();
        DdipEvent ddipEvent = DdipEvent.builder()
                .id(UUID.randomUUID())
                .requesterId(requesterId)
                .title(requestDto.title())
                .content(requestDto.content())
                .reward(requestDto.reward())
                .latitude(requestDto.latitude())
                .longitude(requestDto.longitude())
                .difficulty(requestDto.difficulty())
                .status(DdipStatus.OPEN)
                .createdAt(Instant.now())
                .photos(new ArrayList<>())
                .interactions(new ArrayList<>())
                .applicants(new ArrayList<>())
                .build();

        given(userRepository.getById(requesterId)).willReturn(userEntityDto);
        given(ddipEventRepository.save(any(DdipEvent.class))).willReturn(ddipEvent);

        // when
        DdipEventDetailDto result = ddipService.createDdipEvent(requestDto, requesterId);

        // then
        assertThat(result.title()).isEqualTo(requestDto.title());
        assertThat(result.content()).isEqualTo(requestDto.content());
        verify(userRepository).getById(requesterId);
        verify(ddipEventRepository).save(any(DdipEvent.class));
    }

    @DisplayName("띱 피드 조회 성공")
    @Test
    void givenFeedRequest_whenGetDdipEventFeed_thenListOfDdipEventSummaryDtoIsReturned() {
        // given
        FeedRequestDto requestDto = new FeedRequestDto(35.0, 128.0, 36.0, 129.0, "distance", 35.5, 128.5);
        DdipEvent ddipEvent = DdipEvent.builder()
                .id(UUID.randomUUID())
                .requesterId(UUID.randomUUID())
                .createdAt(Instant.now())
                .applicants(new ArrayList<>())
                .latitude(0.0)
                .longitude(0.0)
                .build();
        List<DdipEvent> events = List.of(ddipEvent);

        given(ddipEventRepository.findWithinBounds(
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                anyString(), anyDouble(), anyDouble())).willReturn(events);

        // when
        List<DdipEventSummaryDto> result = ddipService.getDdipEventFeed(requestDto);

        // then
        assertThat(result).hasSize(1);
        verify(ddipEventRepository).findWithinBounds(
                requestDto.sw_lat(), requestDto.sw_lon(), requestDto.ne_lat(), requestDto.ne_lon(),
                requestDto.sort(), requestDto.user_lat(), requestDto.user_lon());
    }

    @DisplayName("띱 상세 조회 성공")
    @Test
    void givenDdipId_whenGetDdipEventDetail_thenDdipEventDetailDtoIsReturned() {
        // given
        UUID eventId = UUID.randomUUID();
        DdipEvent ddipEvent = DdipEvent.builder()
                .id(eventId)
                .requesterId(UUID.randomUUID())
                .createdAt(Instant.now())
                .photos(new ArrayList<>())
                .interactions(new ArrayList<>())
                .applicants(new ArrayList<>())
                .build();

        given(ddipEventRepository.findById(eventId)).willReturn(Optional.of(ddipEvent));

        // when
        DdipEventDetailDto result = ddipService.getDdipEventDetail(eventId);

        // then
        assertThat(result.id()).isEqualTo(eventId.toString());
        verify(ddipEventRepository).findById(eventId);
    }

    @DisplayName("띱 상세 조회 실패 - 띱을 찾을 수 없음")
    @Test
    void givenInvalidDdipId_whenGetDdipEventDetail_thenDdipNotFoundExceptionIsThrown() {
        // given
        UUID invalidDdipId = UUID.randomUUID();
        given(ddipEventRepository.findById(invalidDdipId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ddipService.getDdipEventDetail(invalidDdipId))
                .isInstanceOf(DdipNotFoundException.class);
        verify(ddipEventRepository).findById(invalidDdipId);
    }
}
