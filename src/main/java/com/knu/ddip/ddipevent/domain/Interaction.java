package com.knu.ddip.ddipevent.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class Interaction {
    private final UUID interactionId;
    private final UUID actorId;
    private final ActorRole actorRole;
    private final ActionType actionType;
    private final MessageCode messageCode;
    private final UUID relatedPhotoId;
    private final Instant timestamp;
}
