package com.knu.ddip.location.application.service;

import com.knu.ddip.config.IntegrationTestConfig;
import com.knu.ddip.config.MySQLTestContainerConfig;
import com.knu.ddip.config.RedisTestContainerConfig;
import com.knu.ddip.config.TestEnvironmentConfig;
import com.knu.ddip.location.application.dto.UpdateMyLocationRequest;
import com.knu.ddip.location.application.util.S2Converter;
import com.knu.ddip.location.application.util.UuidBase64Utils;
import com.knu.ddip.location.infrastructure.entity.LocationEntity;
import com.knu.ddip.location.infrastructure.repositoroy.LocationJpaRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.knu.ddip.location.application.util.LocationKeyFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith({RedisTestContainerConfig.class, MySQLTestContainerConfig.class, TestEnvironmentConfig.class})
@Import(IntegrationTestConfig.class)
class LocationServiceTest {

    @Autowired
    LocationService locationService;
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    LocationJpaRepository locationJpaRepository;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void saveUserLocationTest() {
        // given
        UUID userId = UUID.randomUUID();
        String encodedUserId = UuidBase64Utils.uuidToBase64String(userId);

        int level = 17;
        double lat = 35.8889737;
        double lng = 128.6099251;

        String cellId = S2Converter.toCellId(lat, lng, level).toToken();

        UpdateMyLocationRequest request = UpdateMyLocationRequest.of(lat, lng);

        // when
        locationService.saveUserLocationAtomic(userId, request);

        // then
        // user:{userId} 확인
        String userIdKey = "user:" + encodedUserId;
        String saveCellId = (String) redisTemplate.opsForValue().get(userIdKey);
        assertThat(saveCellId).isEqualTo(cellId);
        // set 확인
        String cellIdUsersKey = createCellIdUsersKey(cellId);
        assertThat(redisTemplate.opsForSet().isMember(cellIdUsersKey, encodedUserId)).isTrue();
        // zset 확인
        String cellIdExpiriesKey = createCellIdExpiriesKey(cellId);
        assertThat(redisTemplate.opsForZSet().score(cellIdExpiriesKey, encodedUserId)).isNotNull();
    }

    @DisplayName("이전 유저 위치 정보 삭제 후 저장")
    @Test
    void saveUserLocationRemovesOldLocationAndSavesNewOne() {
        // given
        UUID userId = UUID.randomUUID();
        String encodedUserId = UuidBase64Utils.uuidToBase64String(userId);

        int level = 17;
        double lat = 35.8889737;
        double lng = 128.6099251;

        String cellId = S2Converter.toCellId(lat, lng, level).toToken();

        UpdateMyLocationRequest request = UpdateMyLocationRequest.of(lat, lng);

        locationService.saveUserLocationAtomic(userId, request);

        double newLat = 35.8929024;
        double newLng = 128.6122855;

        UpdateMyLocationRequest newRequest = UpdateMyLocationRequest.of(newLat, newLng);

        // when
        locationService.saveUserLocationAtomic(userId, newRequest);

        // then
        // user:{userId} 삭제 확인
        String userIdKey = createUserIdKey(encodedUserId);
        String saveCellId = (String) redisTemplate.opsForValue().get(userIdKey);
        assertThat(saveCellId).isNotEqualTo(cellId);
        // set 삭제 확인
        String cellIdUsersKey = createCellIdUsersKey(cellId);
        assertThat(redisTemplate.opsForSet().isMember(cellIdUsersKey, encodedUserId)).isFalse();
    }

    @DisplayName("예전 위치가 존재하지만 현재 위치와 같으면 바로 리턴")
    @Test
    void saveUserLocationWithSameOldLocation() {
        // given
        UUID userId = UUID.randomUUID();
        String encodedUserId = UuidBase64Utils.uuidToBase64String(userId);

        int level = 17;
        double lat = 35.8889737;
        double lng = 128.6099251;

        String cellId = S2Converter.toCellId(lat, lng, level).toToken();

        UpdateMyLocationRequest request = UpdateMyLocationRequest.of(lat, lng);

        locationService.saveUserLocationAtomic(userId, request);

        UpdateMyLocationRequest newRequest = UpdateMyLocationRequest.of(lat, lng);

        // when
        locationService.saveUserLocationAtomic(userId, newRequest);

        // then
        // user:{userId} 삭제 확인
        String userIdKey = createUserIdKey(encodedUserId);
        String saveCellId = (String) redisTemplate.opsForValue().get(userIdKey);
        assertThat(saveCellId).isEqualTo(cellId);
        // set 삭제 확인
        String cellIdUsersKey = createCellIdUsersKey(cellId);
        assertThat(redisTemplate.opsForSet().isMember(cellIdUsersKey, encodedUserId)).isTrue();
    }

    @Test
    void getNeighborRecipientUserIdsTest() {
        // given
        UUID myUserId = UUID.randomUUID();

        // 일청담
        double requestLat = 35.8886597;
        double requestLng = 128.612138;

        UpdateMyLocationRequest request = UpdateMyLocationRequest.of(requestLat, requestLng);
        locationService.saveUserLocationAtomic(myUserId, request);

        double[][] latsLngs = {
                {35.8891866, 128.6121152}, // 포함 : 시계탑
                {35.8888694, 128.6115197}, // 포함 : 테니스장 우측
                {35.8885521, 128.6126731}, // 포함 : 일청담 왼쪽 갈림길
                {35.8879915, 128.6066381}, // 제외 : 축구장
                {35.8917529, 128.6122976}, // 제외 : 도서관
        };

        List<UUID> userIds = new ArrayList<>();
        for (int i = 0; i < latsLngs.length; i++) {
            UUID userId = UUID.randomUUID();
            if (i < 3) {
                userIds.add(userId);
            }
            double[] data = latsLngs[i];
            request = UpdateMyLocationRequest.of(data[0], data[1]);
            locationService.saveUserLocationAtomic(userId, request);
        }

        // when
        List<UUID> neighborRecipientUserIds = locationService.getNeighborRecipientUserIds(myUserId, requestLat, requestLng);

        // then
        assertThat(neighborRecipientUserIds).containsAll(userIds);
        assertThat(neighborRecipientUserIds).doesNotContain(myUserId);
    }

}