package com.knu.ddip.location.infrastructure.repository;

import com.knu.ddip.location.application.service.LocationWriter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.knu.ddip.location.application.util.LocationKeyFactory.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LocationWriterImpl implements LocationWriter {

    private final LocationJpaRepository locationJpaRepository;
    private final JdbcTemplate jdbcTemplate;
    private final RedisConnectionFactory connectionFactory;
    private final RedisTemplate<String, String> redisTemplate;

    public static final long TTL_SECONDS = 3600L;

    private DefaultRedisScript<String> saveUserLocationScript;

    @PostConstruct
    public void init() {
        saveUserLocationScript = new DefaultRedisScript<>();
        saveUserLocationScript.setResultType(String.class);
        saveUserLocationScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("luascript/save_user_location.lua"))
        );
    }

    @Override
    public void deleteAll() {
        locationJpaRepository.deleteAllInBatch();
    }

    @Override
    public void saveAll(List<String> cellIds) {
        String sql = """
                INSERT INTO locations (cell_id) VALUES (?)
                """;

        jdbcTemplate.batchUpdate(sql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        String cellId = cellIds.get(i);
                        ps.setString(1, cellId);
                    }

                    @Override
                    public int getBatchSize() {
                        return cellIds.size();
                    }
                });
    }

    @Override
    public void saveUserIdByCellIdAtomic(String newCellId, boolean cellIdNotInTargetArea, String encodedUserId) {
        String userIdKey = createUserIdKey(encodedUserId);
        String cellIdUsersKey = createCellIdUsersKey(newCellId);
        String cellIdExpiriesKey = createCellIdExpiriesKey(newCellId);

        long now = System.currentTimeMillis();
        long expireAt = now + TTL_SECONDS * 1000L;
        int cellIdNotInTargetAreaFlag = cellIdNotInTargetArea ? 1 : 0;

        redisTemplate.execute(
                saveUserLocationScript,
                Arrays.asList(userIdKey, cellIdExpiriesKey, cellIdUsersKey),
                newCellId, encodedUserId, String.valueOf(TTL_SECONDS), String.valueOf(expireAt), String.valueOf(cellIdNotInTargetAreaFlag)
        );
    }

    @Override
    public void cleanupExpiredUserLocations(long now) {
        log.info("start cleanupExpiredUserLocations task");
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
            log.info("finish cleanupExpiredUserLocations task");
        } catch (Exception e) {
            log.error("cleanupExpiredUserLocations error", e);
        }
    }
}
