package com.knu.ddip.ddipevent.infrastructure;

import com.knu.ddip.ddipevent.domain.*;
import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;
import com.knu.ddip.ddipevent.infrastructure.entity.InteractionEntity;
import com.knu.ddip.ddipevent.infrastructure.entity.PhotoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DdipMapperTest {

    private DdipMapper ddipMapper;

    @BeforeEach
    void setUp() {
        ddipMapper = new DdipMapper();
    }

    @DisplayName("도메인을 엔티티로 변환 - 모든 필드 포함")
    @Test
    void givenDdipEventDomainWithLists_whenToEntity_thenDdipEventEntityIsReturned() {
        // given
        Photo photo = Photo.builder().photoId(UUID.randomUUID()).build();
        Interaction interaction = Interaction.builder().interactionId(UUID.randomUUID()).build();
        DdipEvent domain = DdipEvent.builder()
                .id(UUID.randomUUID())
                .photos(List.of(photo))
                .interactions(List.of(interaction))
                .latitude(0.0)
                .longitude(0.0)
                .build();

        // when
        DdipEventEntity entity = ddipMapper.toEntity(domain);

        // then
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getPhotos()).hasSize(1);
        assertThat(entity.getPhotos().get(0).getId()).isEqualTo(photo.getPhotoId());
        assertThat(entity.getInteractions()).hasSize(1);
        assertThat(entity.getInteractions().get(0).getId()).isEqualTo(interaction.getInteractionId());
    }

    @DisplayName("도메인을 엔티티로 변환 - Null 리스트")
    @Test
    void givenDdipEventDomainWithNullLists_whenToEntity_thenDdipEventEntityIsReturnedWithEmptyLists() {
        // given
        DdipEvent domain = DdipEvent.builder()
                .id(UUID.randomUUID())
                .photos(null)
                .interactions(null)
                .latitude(0.0)
                .longitude(0.0)
                .build();

        // when
        DdipEventEntity entity = ddipMapper.toEntity(domain);

        // then
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getPhotos()).isNotNull().isEmpty();
        assertThat(entity.getInteractions()).isNotNull().isEmpty();
    }

    @DisplayName("엔티티를 도메인으로 변환 - 모든 필드 포함")
    @Test
    void givenDdipEventEntityWithLists_whenToDomain_thenDdipEventDomainIsReturned() {
        // given
        PhotoEntity photoEntity = PhotoEntity.builder().id(UUID.randomUUID()).build();
        InteractionEntity interactionEntity = InteractionEntity.builder().id(UUID.randomUUID()).build();
        DdipEventEntity entity = DdipEventEntity.builder()
                .id(UUID.randomUUID())
                .photos(List.of(photoEntity))
                .interactions(List.of(interactionEntity))
                .build();

        // when
        DdipEvent domain = ddipMapper.toDomain(entity);

        // then
        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getPhotos()).hasSize(1);
        assertThat(domain.getPhotos().get(0).getPhotoId()).isEqualTo(photoEntity.getId());
        assertThat(domain.getInteractions()).hasSize(1);
        assertThat(domain.getInteractions().get(0).getInteractionId()).isEqualTo(interactionEntity.getId());
    }

    @DisplayName("엔티티를 도메인으로 변환 - Null 리스트")
    @Test
    void givenDdipEventEntityWithNullLists_whenToDomain_thenDdipEventDomainIsReturnedWithEmptyLists() {
        // given
        DdipEventEntity entity = DdipEventEntity.builder()
                .id(UUID.randomUUID())
                .photos(null)
                .interactions(null)
                .build();

        // when
        DdipEvent domain = ddipMapper.toDomain(entity);

        // then
        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getPhotos()).isNotNull().isEmpty();
        assertThat(domain.getInteractions()).isNotNull().isEmpty();
    }

    @DisplayName("사진 도메인을 엔티티로 매핑 - 모든 필드 포함")
    @Test
    void givenPhotoWithAllFields_whenMapToEntity_thenAllFieldsAreMapped() {
        // given
        UUID photoId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Photo photo = Photo.builder()
                .photoId(photoId)
                .photoUrl("https://example.com/photo.jpg")
                .latitude(35.888)
                .longitude(128.61)
                .timestamp(timestamp)
                .status(PhotoStatus.PENDING)
                .responderComment("수행자 코멘트")
                .requesterQuestion("요청자 질문")
                .responderAnswer("수행자 답변")
                .rejectionReason("거절 사유")
                .build();
        
        DdipEvent domain = DdipEvent.builder()
                .photos(List.of(photo))
                .latitude(0.0)
                .longitude(0.0)
                .build();

        // when
        DdipEventEntity entity = ddipMapper.toEntity(domain);

        // then
        PhotoEntity mappedPhoto = entity.getPhotos().get(0);
        assertThat(mappedPhoto.getId()).isEqualTo(photoId);
        assertThat(mappedPhoto.getPhotoUrl()).isEqualTo("https://example.com/photo.jpg");
        assertThat(mappedPhoto.getLatitude()).isEqualTo(35.888);
        assertThat(mappedPhoto.getLongitude()).isEqualTo(128.61);
        assertThat(mappedPhoto.getTimestamp()).isEqualTo(timestamp);
        assertThat(mappedPhoto.getStatus()).isEqualTo(PhotoStatus.PENDING);
        assertThat(mappedPhoto.getResponderComment()).isEqualTo("수행자 코멘트");
        assertThat(mappedPhoto.getRequesterQuestion()).isEqualTo("요청자 질문");
        assertThat(mappedPhoto.getResponderAnswer()).isEqualTo("수행자 답변");
        assertThat(mappedPhoto.getRejectionReason()).isEqualTo("거절 사유");
        assertThat(mappedPhoto.getDdipEvent()).isEqualTo(entity);
    }

    @DisplayName("인터랙션 도메인을 엔티티로 매핑 - 모든 필드 포함")
    @Test
    void givenInteractionWithAllFields_whenMapToEntity_thenAllFieldsAreMapped() {
        // given
        UUID interactionId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID relatedPhotoId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        Interaction interaction = Interaction.builder()
                .interactionId(interactionId)
                .actorId(actorId)
                .actorRole(ActorRole.REQUESTER)
                .actionType(ActionType.ASK_QUESTION)
                .comment("테스트 코멘트")
                .relatedPhotoId(relatedPhotoId)
                .timestamp(timestamp)
                .build();
        
        DdipEvent domain = DdipEvent.builder()
                .interactions(List.of(interaction))
                .latitude(0.0)
                .longitude(0.0)
                .build();

        // when
        DdipEventEntity entity = ddipMapper.toEntity(domain);

        // then
        InteractionEntity mappedInteraction = entity.getInteractions().get(0);
        assertThat(mappedInteraction.getId()).isEqualTo(interactionId);
        assertThat(mappedInteraction.getActorId()).isEqualTo(actorId);
        assertThat(mappedInteraction.getActorRole()).isEqualTo(ActorRole.REQUESTER);
        assertThat(mappedInteraction.getActionType()).isEqualTo(ActionType.ASK_QUESTION);
        assertThat(mappedInteraction.getContent()).isEqualTo("테스트 코멘트");
        assertThat(mappedInteraction.getRelatedPhotoId()).isEqualTo(relatedPhotoId);
        assertThat(mappedInteraction.getTimestamp()).isEqualTo(timestamp);
        assertThat(mappedInteraction.getDdipEvent()).isEqualTo(entity);
    }

    @DisplayName("사진 엔티티를 도메인으로 매핑 - 모든 필드 포함")
    @Test
    void givenPhotoEntityWithAllFields_whenMapToDomain_thenAllFieldsAreMapped() {
        // given
        UUID photoId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        PhotoEntity photoEntity = PhotoEntity.builder()
                .id(photoId)
                .photoUrl("https://example.com/photo.jpg")
                .latitude(35.888)
                .longitude(128.61)
                .timestamp(timestamp)
                .status(PhotoStatus.APPROVED)
                .responderComment("수행자 코멘트")
                .requesterQuestion("요청자 질문")
                .responderAnswer("수행자 답변")
                .rejectionReason("거절 사유")
                .build();
        
        DdipEventEntity entity = DdipEventEntity.builder()
                .photos(List.of(photoEntity))
                .build();

        // when
        DdipEvent domain = ddipMapper.toDomain(entity);

        // then
        Photo mappedPhoto = domain.getPhotos().get(0);
        assertThat(mappedPhoto.getPhotoId()).isEqualTo(photoId);
        assertThat(mappedPhoto.getPhotoUrl()).isEqualTo("https://example.com/photo.jpg");
        assertThat(mappedPhoto.getLatitude()).isEqualTo(35.888);
        assertThat(mappedPhoto.getLongitude()).isEqualTo(128.61);
        assertThat(mappedPhoto.getTimestamp()).isEqualTo(timestamp);
        assertThat(mappedPhoto.getStatus()).isEqualTo(PhotoStatus.APPROVED);
        assertThat(mappedPhoto.getResponderComment()).isEqualTo("수행자 코멘트");
        assertThat(mappedPhoto.getRequesterQuestion()).isEqualTo("요청자 질문");
        assertThat(mappedPhoto.getResponderAnswer()).isEqualTo("수행자 답변");
        assertThat(mappedPhoto.getRejectionReason()).isEqualTo("거절 사유");
    }

    @DisplayName("인터랙션 엔티티를 도메인으로 매핑 - 모든 필드 포함")
    @Test
    void givenInteractionEntityWithAllFields_whenMapToDomain_thenAllFieldsAreMapped() {
        // given
        UUID interactionId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID relatedPhotoId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        InteractionEntity interactionEntity = InteractionEntity.builder()
                .id(interactionId)
                .actorId(actorId)
                .actorRole(ActorRole.RESPONDER)
                .actionType(ActionType.ANSWER_QUESTION)
                .content("테스트 코멘트")
                .relatedPhotoId(relatedPhotoId)
                .timestamp(timestamp)
                .build();
        
        DdipEventEntity entity = DdipEventEntity.builder()
                .interactions(List.of(interactionEntity))
                .build();

        // when
        DdipEvent domain = ddipMapper.toDomain(entity);

        // then
        Interaction mappedInteraction = domain.getInteractions().get(0);
        assertThat(mappedInteraction.getInteractionId()).isEqualTo(interactionId);
        assertThat(mappedInteraction.getActorId()).isEqualTo(actorId);
        assertThat(mappedInteraction.getActorRole()).isEqualTo(ActorRole.RESPONDER);
        assertThat(mappedInteraction.getActionType()).isEqualTo(ActionType.ANSWER_QUESTION);
        assertThat(mappedInteraction.getComment()).isEqualTo("테스트 코멘트");
        assertThat(mappedInteraction.getRelatedPhotoId()).isEqualTo(relatedPhotoId);
        assertThat(mappedInteraction.getTimestamp()).isEqualTo(timestamp);
    }
}
