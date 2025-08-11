package com.knu.ddip.location.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.geometry.S2CellId;
import com.knu.ddip.location.application.dto.UpdateMyLocationRequest;
import com.knu.ddip.location.application.util.S2Converter;
import com.knu.ddip.location.application.util.UuidBase64Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    private final ObjectMapper objectMapper;

    public static final int LEVEL = 17;
    public static final String KNU_GEOJSON_FEATURE_FILENAME = "geojson/cells.geojson";

    // KNU GeoJSON 파일을 읽어서 각 Feature를 DB에 저장
    @Transactional
    public void loadAndSaveGeoJsonFeatures() {
        try {
            // resources 폴더에서 파일 읽기
            ClassPathResource resource = new ClassPathResource(KNU_GEOJSON_FEATURE_FILENAME);
            String geoJsonContent = new String(Files.readAllBytes(resource.getFile().toPath()));

            // 기존 데이터 삭제
            locationRepository.deleteAll();

            // JSON 파싱
            JsonNode rootNode = objectMapper.readTree(geoJsonContent);
            JsonNode featuresNode = rootNode.get("features");
            List<String> cellIds = StreamSupport.stream(featuresNode.spliterator(), false)
                    .map(featureNode -> featureNode.get("properties").get("id").asText())
                    .collect(Collectors.toList());

            locationRepository.saveAll(cellIds);

            log.info("총 {}개의 S2Cell Feature가 저장되었습니다.", cellIds.size());
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    public void saveUserLocation(UUID userId, UpdateMyLocationRequest request) {
        S2CellId cellIdObj = S2Converter.toCellId(request.lat(), request.lng(), LEVEL);
        String cellId = cellIdObj.toToken();

        String encodedUserId = UuidBase64Utils.uuidToBase64String(userId);

        // 이전 유저 위치 정보 삭제 후 저장
        // 예전 위치 있으면 -> 현재 위치와 다르면 삭제 후 저장
        // 예전 위치 있으면 -> 현재 위치와 같으면 바로 리턴
        // 예전 위치 없으면 -> 저장

        // 예전 위치 있으면
        Optional<String> cellIdByUserIdOptional = locationRepository.findCellIdByUserId(encodedUserId);
        if (cellIdByUserIdOptional.isPresent()) {
            String cellIdByUserId = cellIdByUserIdOptional.get();
            // 현재 위치와 같으면 바로 리턴
            if (cellId.equals(cellIdByUserId)) return;
            // 이전 위치 삭제
            locationRepository.deleteUserIdByCellId(encodedUserId, cellIdByUserId);
        }

        // 경북대 내부에 위치하는지 확인
        locationRepository.validateLocationByCellId(cellId);

        // 현재 위치 저장
        // 유저의 현재 cellId 저장
        locationRepository.saveCellIdByUserId(encodedUserId, cellId);
        // 현재 cellId에 포함된 유저 저장
        locationRepository.saveUserIdByCellId(encodedUserId, cellId);
    }

    public List<UUID> getNeighborRecipientUserIds(UUID myUserId, double lat, double lng) {
        S2CellId cellIdObj = S2Converter.toCellId(lat, lng, LEVEL);
        String cellId = cellIdObj.toToken();

        // 경북대 내부에 위치하는지 확인
        locationRepository.validateLocationByCellId(cellId);

        // 이웃 cellIds 가져오기
        List<S2CellId> neighbors = new ArrayList<>();
        cellIdObj.getAllNeighbors(LEVEL, neighbors);
        List<String> neighborCellIds = neighbors.stream()
                .map(S2CellId::toToken)
                .collect(Collectors.toList());

        // 경북대 내부에 위치하는 이웃 cellIds만 가져오기
        List<String> targetCellIds = locationRepository.findAllLocationsByCellIdIn(neighborCellIds);
        targetCellIds.add(cellId);

        // targetCellId의 userIds만 가져오기
        List<UUID> userIds = targetCellIds.stream()
                .map(locationRepository::findUserIdsByCellId)
                .flatMap(List::stream)
                .map(UuidBase64Utils::base64StringToUuid)
                .filter(userId -> !userId.equals(myUserId))
                .collect(Collectors.toList());

        return userIds;
    }
}
