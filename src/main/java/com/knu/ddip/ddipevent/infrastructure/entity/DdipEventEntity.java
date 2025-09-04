package com.knu.ddip.ddipevent.infrastructure.entity;

import com.knu.ddip.ddipevent.domain.DdipStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ddip_event")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DdipEventEntity {
    @Id
    @UuidGenerator
    @Column(columnDefinition = "char(36)", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(columnDefinition = "char(36)", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID requesterId;

    @Column(nullable = false)
    private Integer reward;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    @Setter
    private String cellId;

    @Column(nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DdipStatus status;

    @Column(columnDefinition = "char(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID selectedResponderId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "ddip_applicant", joinColumns = @JoinColumn(name = "ddip_event_id"))
    @Column(name = "applicant_id", columnDefinition = "char(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private List<UUID> applicants;

    @Setter
    @OneToMany(mappedBy = "ddipEvent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PhotoEntity> photos;

    @Setter
    @OneToMany(mappedBy = "ddipEvent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InteractionEntity> interactions;

    @Column(nullable = false)
    private Integer difficulty;
}
