package com.knu.ddip.ddipevent.infrastructure;

import com.knu.ddip.ddipevent.domain.DdipEvent;
import com.knu.ddip.ddipevent.domain.Interaction;
import com.knu.ddip.ddipevent.domain.Photo;
import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;
import com.knu.ddip.ddipevent.infrastructure.entity.InteractionEntity;
import com.knu.ddip.ddipevent.infrastructure.entity.PhotoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
