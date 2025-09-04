package com.knu.ddip.location.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.geometry.*;
import com.knu.ddip.location.application.dto.UpdateMyLocationRequest;
import com.knu.ddip.location.application.util.S2Constants;
import com.knu.ddip.location.application.util.S2Converter;
import com.knu.ddip.location.application.util.UuidBase64Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.knu.ddip.location.application.util.S2Constants.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LocationService {

    private final LocationReader locationReader;
    private final LocationWriter locationWriter;

    private final S2Converter s2Converter;

    private final ObjectMapper objectMapper;

    public static final String KNU_GEOJSON_FEATURE_FILENAME = "geojson/cells.geojson";

    // KNU GeoJSON 파일을 읽어서 각 Feature를 DB에 저장
    @Transactional
    public void loadAndSaveGeoJsonFeatures() {
        // GeoJson 파일 읽기
        String geoJsonContent = getGeoJsonContent();

        try {
            // 기존 데이터 삭제
            locationWriter.deleteAll();

            // JSON 파싱
            JsonNode rootNode = objectMapper.readTree(geoJsonContent);
            JsonNode featuresNode = rootNode.get("features");
            List<String> cellIds = StreamSupport.stream(featuresNode.spliterator(), false)
                    .map(featureNode -> featureNode.get("properties").get("id").asText())
                    .collect(Collectors.toList());

            locationWriter.saveAll(cellIds);

            log.info("총 {}개의 S2Cell Feature가 저장되었습니다.", cellIds.size());
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    public void saveUserLocationAtomic(UUID userId, UpdateMyLocationRequest request) {
        S2CellId cellIdObj = s2Converter.toCellId(request.lat(), request.lng());
        String cellId = cellIdObj.toToken();

        // 경북대 내부에 위치하는지 확인
        boolean cellIdNotInTargetArea = locationReader.isCellIdNotInTargetArea(cellId);

        String encodedUserId = UuidBase64Utils.uuidToBase64String(userId);

        locationWriter.saveUserIdByCellIdAtomic(cellId, cellIdNotInTargetArea, encodedUserId);
    }

    // 요청 전송 시 이웃 userIds 조회
    public List<UUID> getNeighborRecipientUserIds(UUID myUserId, double lat, double lng) {
        S2CellId cellIdObj = s2Converter.toCellId(lat, lng);
        String cellId = cellIdObj.toToken();

        // 경북대 내부에 위치하는지 확인
        locationReader.validateLocationByCellId(cellId);

        // 이웃 cellIds 가져오기
        List<S2CellId> neighbors = new ArrayList<>();
        cellIdObj.getAllNeighbors(LEVEL, neighbors);
        List<String> neighborCellIds = neighbors.stream()
                .map(S2CellId::toToken)
                .collect(Collectors.toList());

        // 경북대 내부에 위치하는 이웃 cellIds만 가져오기
        List<String> targetCellIds = locationReader.findAllLocationsByCellIdIn(neighborCellIds);
        targetCellIds.add(cellId);

        // targetCellId의 userIds만 가져오기

        List<UUID> userIds = locationReader.findUserIdsByCellIds(targetCellIds).stream()
                .map(UuidBase64Utils::base64StringToUuid)
                .filter(userId -> !userId.equals(myUserId))
                .collect(Collectors.toList());

        return userIds;
    }

    // 띱 요청 보낼 대상 인접 셀 조회
    public List<String> getNeighborCellIdsToSendDdipRequest(double lat, double lng) {
        S2CellId cellIdObj = s2Converter.toCellId(lat, lng);
        String cellId = cellIdObj.toToken();

        // 경북대 내부에 위치하는지 확인
        locationReader.validateLocationByCellId(cellId);

        // 이웃 cellIds 가져오기
        List<S2CellId> neighbors = new ArrayList<>();
        cellIdObj.getAllNeighbors(LEVEL, neighbors);
        List<String> neighborCellIds = neighbors.stream()
                .map(S2CellId::toToken)
                .collect(Collectors.toList());

        // 경북대 내부에 위치하는 이웃 cellIds만 가져오기
        List<String> targetCellIds = locationReader.findAllLocationsByCellIdIn(neighborCellIds);
        targetCellIds.add(cellId);

        return targetCellIds;
    }

    // 근처 띱 요청 조회용 셀 조회
    public List<String> getNeighborCellIdsToRetrieveNearDdipRequest(double minLat, double minLng, double maxLat, double maxLng) {
        S2LatLngRect rect = S2LatLngRect.fromPointPair(
                S2LatLng.fromDegrees(minLat, minLng),
                S2LatLng.fromDegrees(maxLat, maxLng)
        );

        S2RegionCoverer coverer = S2RegionCoverer.builder()
                .setMinLevel(LEVEL)
                .setMaxLevel(LEVEL)
                .build();

        S2CellUnion union = new S2CellUnion();
        coverer.getCovering(rect, union);

        List<String> cellIds = normalizeCellLevel(union, LEVEL);

        List<String> filteredCellIds = locationReader.findAllLocationsByCellIdIn(cellIds);
        return filteredCellIds;
    }

    private String getGeoJsonContent() {
        ClassPathResource resource = new ClassPathResource(KNU_GEOJSON_FEATURE_FILENAME);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> normalizeCellLevel(S2CellUnion union, int level) {
        List<String> out = new ArrayList<>();
        for (S2CellId cid : union.cellIds()) {
            if (cid.level() == level) {
                out.add(cid.toToken());
            } else if (cid.level() < level) {
                S2CellId begin = cid.childBegin(level);
                S2CellId end = cid.childEnd(level);
                for (S2CellId it = begin; !it.equals(end); it = it.next()) {
                    out.add(it.toToken());
                }
            } else {
                out.add(cid.parent(level).toToken());
            }
        }
        return out;
    }
}
