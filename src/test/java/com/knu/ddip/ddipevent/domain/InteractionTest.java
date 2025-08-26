package com.knu.ddip.ddipevent.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InteractionTest {

    @DisplayName("상호작용 생성 성공")
    @Test
    void givenInteractionInfo_whenCreate_thenInteractionIsCreated() {
        // given
        UUID interactionId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        ActorRole actorRole = ActorRole.REQUESTER;
        ActionType actionType = ActionType.APPLY;
        MessageCode messageCode = MessageCode.GREAT_SENSE;
        UUID relatedPhotoId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        // when
        Interaction interaction = Interaction.builder()
                .interactionId(interactionId)
                .actorId(actorId)
                .actorRole(actorRole)
                .actionType(actionType)
                .messageCode(messageCode)
                .relatedPhotoId(relatedPhotoId)
                .timestamp(timestamp)
                .build();

        // then
        assertThat(interaction.getInteractionId()).isEqualTo(interactionId);
        assertThat(interaction.getActorId()).isEqualTo(actorId);
        assertThat(interaction.getActorRole()).isEqualTo(actorRole);
        assertThat(interaction.getActionType()).isEqualTo(actionType);
        assertThat(interaction.getMessageCode()).isEqualTo(messageCode);
        assertThat(interaction.getRelatedPhotoId()).isEqualTo(relatedPhotoId);
        assertThat(interaction.getTimestamp()).isEqualTo(timestamp);
    }
}
