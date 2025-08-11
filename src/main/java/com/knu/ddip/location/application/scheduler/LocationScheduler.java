package com.knu.ddip.location.application.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class LocationScheduler {

    private final RedisTemplate redisTemplate;

    @Scheduled(cron = "0 0 * * * *") // 매 정시
    public void cleanupExpiredUserLocations() {
        long now = System.currentTimeMillis();
        ScanOptions scan = ScanOptions.scanOptions()
                .match("cell:*:expiry")
                .build();

        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();

        if (connectionFactory == null) return;

        try (
                RedisConnection conn = connectionFactory.getConnection();
                Cursor<byte[]> cursor = conn.keyCommands().scan(scan)
        ) {
            while (cursor.hasNext()) {
                // 1. key 생성
                byte[] expiryKey = cursor.next();
                String expiryKeyStr = new String(expiryKey, StandardCharsets.UTF_8);
                String usersKeyStr = expiryKeyStr.replace(":expiry", ":users");
                byte[] usersKey = usersKeyStr.getBytes(StandardCharsets.UTF_8);

                // 2. 만료된 멤버 수집 (-inf ~ now)
                Set<byte[]> expired = conn.zSetCommands()
                        .zRangeByScore(expiryKey, Double.NEGATIVE_INFINITY, (double) now);
                if (expired == null || expired.isEmpty()) continue;

                // 3. 파이프라인으로 SREM + ZREM
                conn.openPipeline();
                for (byte[] member : expired) {
                    conn.sRem(usersKey, member);                 // SET에서 제거
                    conn.zSetCommands().zRem(expiryKey, member); // ZSET에서도 제거
                }
                conn.closePipeline();
            }
        }
    }

}