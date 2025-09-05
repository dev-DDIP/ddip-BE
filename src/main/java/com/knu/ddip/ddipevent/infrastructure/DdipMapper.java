package com.knu.ddip.ddipevent.infrastructure;

import com.knu.ddip.ddipevent.domain.DdipEvent;
import com.knu.ddip.ddipevent.domain.Interaction;
import com.knu.ddip.ddipevent.domain.Photo;
import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;
import com.knu.ddip.ddipevent.infrastructure.entity.InteractionEntity;
import com.knu.ddip.ddipevent.infrastructure.entity.PhotoEntity;
import com.knu.ddip.location.application.util.S2Converter;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DdipMapper {

    private final S2Converter s2Converter;

    public static final int SRID = 4326;
    private GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);

    public DdipEventEntity toEntity(DdipEvent domain) {
        DdipEventEntity entity = buildDdipEventEntity(domain);
        entity.setPhotos(mapPhotos(domain.getPhotos(), entity));
        entity.setInteractions(mapInteractions(domain.getInteractions(), entity));
        entity.setCellId(s2Converter.toCellIdString(domain.getLatitude(),domain.getLongitude()));
        entity.setLocalPoint(geometryFactory.createPoint(new Coordinate(domain.getLongitude(), domain.getLatitude())));
        return entity;
    }

    private DdipEventEntity buildDdipEventEntity(DdipEvent domain) {
        return DdipEventEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .content(domain.getContent())
                .requesterId(domain.getRequesterId())
                .reward(domain.getReward())
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .createdAt(domain.getCreatedAt())
                .status(domain.getStatus())
                .selectedResponderId(domain.getSelectedResponderId())
                .applicants(domain.getApplicants())
                .difficulty(domain.getDifficulty())
                .build();
    }

    private List<PhotoEntity> mapPhotos(List<Photo> photos, DdipEventEntity ddipEvent) {
        if (photos == null) return List.of();
        return photos.stream()
                .map(photo -> PhotoEntity.builder()
                        .id(photo.getPhotoId())
                        .ddipEvent(ddipEvent)
                        .photoUrl(photo.getPhotoUrl())
                        .latitude(photo.getLatitude())
                        .longitude(photo.getLongitude())
                        .timestamp(photo.getTimestamp())
                        .status(photo.getStatus())
                        .responderComment(photo.getResponderComment())
                        .requesterQuestion(photo.getRequesterQuestion())
                        .responderAnswer(photo.getResponderAnswer())
                        .rejectionReason(photo.getRejectionReason())
                        .build())
                .toList();
    }

    private List<InteractionEntity> mapInteractions(List<Interaction> interactions, DdipEventEntity ddipEvent) {
        if (interactions == null) return List.of();
        return interactions.stream()
                .map(interaction -> InteractionEntity.builder()
                        .id(interaction.getInteractionId())
                        .ddipEvent(ddipEvent)
                        .actorId(interaction.getActorId())
                        .actorRole(interaction.getActorRole())
                        .actionType(interaction.getActionType())
                        .content(interaction.getComment())
                        .relatedPhotoId(interaction.getRelatedPhotoId())
                        .timestamp(interaction.getTimestamp())
                        .build())
                .toList();
    }

    public DdipEvent toDomain(DdipEventEntity entity) {
        return DdipEvent.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .requesterId(entity.getRequesterId())
                .reward(entity.getReward())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .cellId(entity.getCellId())
                .createdAt(entity.getCreatedAt())
                .status(entity.getStatus())
                .selectedResponderId(entity.getSelectedResponderId())
                .applicants(entity.getApplicants())
                .photos(mapPhotoDomain(entity.getPhotos()))
                .interactions(mapInteractionDomain(entity.getInteractions()))
                .difficulty(entity.getDifficulty())
                .build();
    }

    private List<Photo> mapPhotoDomain(List<PhotoEntity> photoEntities) {
        if (photoEntities == null) return List.of();
        return photoEntities.stream()
                .map(pe -> Photo.builder()
                        .photoId(pe.getId())
                        .photoUrl(pe.getPhotoUrl())
                        .latitude(pe.getLatitude())
                        .longitude(pe.getLongitude())
                        .timestamp(pe.getTimestamp())
                        .status(pe.getStatus())
                        .responderComment(pe.getResponderComment())
                        .requesterQuestion(pe.getRequesterQuestion())
                        .responderAnswer(pe.getResponderAnswer())
                        .rejectionReason(pe.getRejectionReason())
                        .build())
                .toList();
    }

    private List<Interaction> mapInteractionDomain(List<InteractionEntity> interactionEntities) {
        if (interactionEntities == null) return List.of();
        return interactionEntities.stream()
                .map(ie -> Interaction.builder()
                        .interactionId(ie.getId())
                        .actorId(ie.getActorId())
                        .actorRole(ie.getActorRole())
                        .actionType(ie.getActionType())
                        .comment(ie.getContent())
                        .relatedPhotoId(ie.getRelatedPhotoId())
                        .timestamp(ie.getTimestamp())
                        .build())
                .toList();
    }
}
