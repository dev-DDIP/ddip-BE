package com.knu.ddip.ddipevent.infrastructure.entity;

import com.knu.ddip.ddipevent.domain.ActionType;
import com.knu.ddip.ddipevent.domain.ActorRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interaction")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionEntity {
    @Id
    @UuidGenerator
    @Column(columnDefinition = "char(36)", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ddip_event_id")
    private DdipEventEntity ddipEvent;

    @Column(columnDefinition = "char(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActorRole actorRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    private String content;

    @Column(columnDefinition = "char(36)", updatable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID relatedPhotoId;

    @Column(nullable = false)
    private Instant timestamp;
}
