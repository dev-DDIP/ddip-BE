package com.knu.ddip.location.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LocationService {

    public static final String KNU_GEOJSON_FEATURE_FILENAME = "geojson/cells.geojson";

    private final LocationRepository locationRepository;

    private final ObjectMapper objectMapper;

    // KNU GeoJSON 파일을 읽어서 각 Feature를 DB에 저장
    @Transactional
    public void loadAndSaveGeoJsonFeatures() {
        try {
            // resources 폴더에서 파일 읽기
            ClassPathResource resource = new ClassPathResource(KNU_GEOJSON_FEATURE_FILENAME);
            String geoJsonContent = new String(Files.readAllBytes(resource.getFile().toPath()));

            // JSON 파싱
            JsonNode rootNode = objectMapper.readTree(geoJsonContent);
            JsonNode featuresNode = rootNode.get("features");

            // 기존 데이터 삭제
            locationRepository.deleteAll();

            List<String> cellIds = new ArrayList<>();
            for (JsonNode featureNode : featuresNode) {
                JsonNode propertiesNode = featureNode.get("properties");
                String cellId = propertiesNode.get("id").asText();
                cellIds.add(cellId);
            }

            locationRepository.saveAll(cellIds);

            log.info("총 {}개의 S2Cell Feature가 저장되었습니다.", cellIds.size());
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }
}
