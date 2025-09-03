package com.knu.ddip.ddipevent.infrastructure.entity;

import com.knu.ddip.ddipevent.domain.PhotoStatus;
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
@Table(name = "photo")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoEntity {
    @Id
    @UuidGenerator
    @Column(columnDefinition = "char(36)", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ddip_event_id")
    private DdipEventEntity ddipEvent;

    @Column(nullable = false)
    private String photoUrl;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhotoStatus status;

    private String responderComment;

    private String requesterQuestion;

    private String responderAnswer;

    private String rejectionReason;
}
