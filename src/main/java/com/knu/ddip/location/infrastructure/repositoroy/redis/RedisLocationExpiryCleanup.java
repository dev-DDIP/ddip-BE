package com.knu.ddip.location.infrastructure.repositoroy.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisLocationExpiryCleanup {

    private final RedisConnectionFactory connectionFactory;

    public void cleanupExpiredUserLocations(long now) {
        try (
                RedisConnection conn = connectionFactory.getConnection();
                Cursor<byte[]> cursor = conn.keyCommands()
                        .scan(ScanOptions.scanOptions()
                                .match("cell:*:expiry")
                                .build())
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
