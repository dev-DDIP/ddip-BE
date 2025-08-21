package com.knu.ddip.location.application.service;

import com.knu.ddip.config.IntegrationTestConfig;
import com.knu.ddip.config.MySQLTestContainerConfig;
import com.knu.ddip.config.RedisTestContainerConfig;
import com.knu.ddip.config.TestEnvironmentConfig;
import com.knu.ddip.location.application.dto.UpdateMyLocationRequest;
import com.knu.ddip.location.application.util.S2Converter;
import com.knu.ddip.location.application.util.UuidBase64Utils;
import com.knu.ddip.location.exception.LocationNotFoundException;
import com.knu.ddip.location.infrastructure.repository.LocationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;

import static com.knu.ddip.location.application.util.LocationKeyFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith({RedisTestContainerConfig.class, MySQLTestContainerConfig.class, TestEnvironmentConfig.class})
@Import(IntegrationTestConfig.class)
class LocationServiceTest {

    public static final int LEVEL = 17;
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

        double lat = 35.8889737;
        double lng = 128.6099251;

        String cellId = S2Converter.toCellId(lat, lng, LEVEL).toToken();

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

        double lat = 35.8889737;
        double lng = 128.6099251;

        String cellId = S2Converter.toCellId(lat, lng, LEVEL).toToken();

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

        double lat = 35.8889737;
        double lng = 128.6099251;

        String cellId = S2Converter.toCellId(lat, lng, LEVEL).toToken();

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

    @Test
    void getNeighborCellIdsNotInTargetAreaTest() {
        // given
        double outsideTargetAreaLat = 1.0;
        double outsideTargetAreaLng = 1.0;

        // when // then
        assertThatThrownBy(() -> locationService.getNeighborCellIds(outsideTargetAreaLat, outsideTargetAreaLng))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("위치를 찾을 수 없습니다.");
    }

    @Test
    void getNeighborCellIdsTest() {
        // given
        double lat = 35.8890084;
        double lng = 128.6107405;

        // when
        List<String> neighborCellIds = locationService.getNeighborCellIds(lat, lng);

        // then
        assertThat(neighborCellIds).hasSize(9);
    }

    @Test
    void getNeighborCellIdsAtEdgeTest() {
        // given
        double lat = 35.8928546;
        double lng = 128.608922;

        // when
        List<String> neighborCellIds = locationService.getNeighborCellIds(lat, lng);

        // then
        assertThat(neighborCellIds).hasSize(6);
    }

    @Test
    void saveUserIdByCellIdTest() {
        // given
        UUID userId = UUID.randomUUID();
        String encodedUserId = UuidBase64Utils.uuidToBase64String(userId);

        String userIdKey = createUserIdKey(encodedUserId);

        double requestLat = 35.8886597;
        double requestLng = 128.612138;

        String cellId = S2Converter.toCellId(requestLat, requestLng, LEVEL).toToken();
        String cellIdUsersKey = createCellIdUsersKey(cellId);
        String cellIdExpiriesKey = createCellIdExpiriesKey(cellId);

        UpdateMyLocationRequest request = UpdateMyLocationRequest.of(requestLat, requestLng);

        // when
        locationService.saveUserLocationAtomic(userId, request);

        // then
        assertThat(redisTemplate.opsForValue().get(userIdKey)).isEqualTo(cellId);
        assertThat(redisTemplate.opsForSet().isMember(cellIdUsersKey, encodedUserId)).isTrue();
        assertThat(redisTemplate.opsForZSet().score(cellIdExpiriesKey, encodedUserId)).isNotNull();
    }

    @Test
    void saveUserIdByCellIdAtOutsideOfTargetAreaTest() {
        // given
        UUID userId = UUID.randomUUID();
        String encodedUserId = UuidBase64Utils.uuidToBase64String(userId);

        String userIdKey = createUserIdKey(encodedUserId);

        double requestLat = 35.8942626;
        double requestLng = 128.6066904;

        String cellId = S2Converter.toCellId(requestLat, requestLng, LEVEL).toToken();
        String cellIdUsersKey = createCellIdUsersKey(cellId);
        String cellIdExpiriesKey = createCellIdExpiriesKey(cellId);

        UpdateMyLocationRequest request = UpdateMyLocationRequest.of(requestLat, requestLng);

        // when
        locationService.saveUserLocationAtomic(userId, request);

        // then
        assertThat(redisTemplate.opsForValue().get(userIdKey)).isNull();
        assertThat(redisTemplate.opsForSet().isMember(cellIdUsersKey, encodedUserId)).isFalse();
        assertThat(redisTemplate.opsForZSet().score(cellIdExpiriesKey, encodedUserId)).isNull();
    }

    @Test
    void saveUserLocationAtomicConcurrencyTest() throws InterruptedException {
        // given
        UUID userId = UUID.randomUUID();

        double[][] latsLngs = {
                {35.8891866, 128.6121152}, // 시계탑
                {35.8888694, 128.6115197}, // 테니스장 우측
                {35.8885521, 128.6126731}, // 일청담 왼쪽 갈림길
                {35.8879915, 128.6066381}, // 축구장
                {35.8917529, 128.6122976}, // 도서관
                {35.8910771, 128.6106976}, // 인문대
                {35.8887477, 128.6137017}, // 박물관
                {35.8880002, 128.6060198}, // 대운동장
                {35.886227, 128.6148282}, // 센트럴파크
                {35.8925547, 128.6143937}, // 간호대
        };

        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());

        int threads = 100;
        int poolSize = 32;

        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        List<Future<?>> futures = new ArrayList<>(threads);
        for (int i = 0; i < threads; i++) {
            int index = rand.nextInt(latsLngs.length);
            double[] coord = latsLngs[index];
            UpdateMyLocationRequest request = UpdateMyLocationRequest.of(coord[0], coord[1]);
            futures.add(pool.submit(() -> {
                try {
                    startLatch.await();
                    locationService.saveUserLocationAtomic(userId, request);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        // when
        startLatch.countDown(); // 동시에 출발
        boolean allDone = doneLatch.await(20, TimeUnit.SECONDS);
        pool.shutdown();
        boolean terminated = pool.awaitTermination(30, TimeUnit.SECONDS);

        String encodedUserId = UuidBase64Utils.uuidToBase64String(userId);
        String userIdKey = createUserIdKey(encodedUserId);

        String cellId = redisTemplate.opsForValue().get(userIdKey);
        String cellIdUsersKey = createCellIdUsersKey(cellId);
        String cellIdExpiriesKey = createCellIdExpiriesKey(cellId);

        // then
        assertThat(allDone).isTrue();
        assertThat(terminated).isTrue();
        assertThat(redisTemplate.opsForSet().isMember(cellIdUsersKey, encodedUserId)).isTrue();
        assertThat(redisTemplate.opsForZSet().score(cellIdExpiriesKey, encodedUserId)).isNotNull();
    }

}