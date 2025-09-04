package com.knu.ddip.ddipevent.application.dto;

import com.knu.ddip.ddipevent.domain.ActionType;
import com.knu.ddip.ddipevent.domain.ActorRole;
import com.knu.ddip.ddipevent.domain.Interaction;

public record InteractionDto(
        String interactionId,
        String actorId,
        ActorRole actorRole,
        ActionType actionType,
        String comment,
        String relatedPhotoId,
        String timestamp
) {
    public static InteractionDto fromEntity(Interaction interaction) {
        return new InteractionDto(
                interaction.getInteractionId().toString(),
                interaction.getActorId().toString(),
                interaction.getActorRole(),
                interaction.getActionType(),
                interaction.getComment(),
                interaction.getRelatedPhotoId() != null
                        ? interaction.getRelatedPhotoId().toString()
                        : null,
                interaction.getTimestamp().toString()
        );
    }
}
