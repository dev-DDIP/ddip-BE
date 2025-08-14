package com.knu.ddip.location.infrastructure.repositoroy;

import com.knu.ddip.location.application.service.LocationRepository;
import com.knu.ddip.location.exception.LocationNotFoundException;
import com.knu.ddip.location.infrastructure.entity.LocationEntity;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {

    public static final long TTL_SECONDS = 3600L;
    private final LocationJpaRepository locationJpaRepository;

    private final JdbcTemplate jdbcTemplate;

    private final RedisTemplate<String, String> redisTemplate;

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
    public void saveUserIdByCellId(String encodedUserId, String cellId) {
        long now = System.currentTimeMillis();
        long expireAt = now + TTL_SECONDS * 1000L;

        redisTemplate.opsForSet().add(createCellIdUsersKey(cellId), encodedUserId);

        // 만료 유저용 zset 저장
        redisTemplate.opsForZSet().add(createCellIdExpiriesKey(cellId), encodedUserId, (double) expireAt);
    }

    @Override
    public void validateLocationByCellId(String cellId) {
        locationJpaRepository.findById(cellId)
                .orElseThrow(() -> new LocationNotFoundException("위치를 찾을 수 없습니다."));
    }

    @Override
    public void saveUserIdByCellIdAtomic(String newCellId, boolean cellIdNotInTargetArea, String encodedUserId) {
        String userIdKey = createUserIdKey(encodedUserId);
        String cellIdUsersKey = createCellIdUsersKey(newCellId);
        String cellIdExpiriesKey = createCellIdExpiriesKey(newCellId);

        long now = System.currentTimeMillis();
        long expireAt = now + TTL_SECONDS * 1000L;

//        String saveUserLocationScript = """
//                -- KEYS:
//                -- [1] userIdKey
//                -- [2] cellIdExpiriesKey
//                -- [3] cellIdUsersKey
//
//                -- ARGV:
//                -- [1] newCellId
//                -- [2] encodedUserId
//                -- [3] TTL_SECONDS
//                -- [4] expireAt
//
//                local newCellId = ARGV[1]
//                local encodedUserId = ARGV[2]
//                local ttl_seconds = tonumber(ARGV[3])
//                local expireAt = tonumber(ARGV[4])
//
//                local prevCellId = redis.call('GET', KEYS[1])
//
//                if prevCellId then
//                    if prevCellId == newCellId then
//                        redis.call('EXPIRE', KEYS[2], ttl_seconds)
//                        return
//                    end
//                end
//
//                redis.call('SREM', KEYS[3], encodedUserId)
//
//                redis.call('SET', KEYS[1], newCellId, 'EX', ttl_seconds)
//
//                redis.call('SADD', KEYS[3], encodedUserId)
//
//                redis.call('ZADD', KEYS[2], expireAt, encodedUserId)
//                """;
//
//        DefaultRedisScript<String> stringDefaultRedisScript = new DefaultRedisScript<>(saveUserLocationScript, String.class);

        redisTemplate.execute(
                saveUserLocationScript,
                Arrays.asList(userIdKey, cellIdExpiriesKey, cellIdUsersKey),
                newCellId, encodedUserId, String.valueOf(TTL_SECONDS), String.valueOf(expireAt)
        );
    }

    @Override
    public List<String> findAllLocationsByCellIdIn(List<String> cellIds) {
        return locationJpaRepository.findAllByCellIdIn(cellIds).stream()
                .map(LocationEntity::getCellId)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findUserIdsByCellIds(List<String> targetCellIds) {
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();

        if (connectionFactory == null) return null; // throw xxx

        try (RedisConnection conn = connectionFactory.getConnection()) {
            conn.openPipeline();

            List<byte[]> keys = targetCellIds.stream()
                    .map(this::createCellIdUsersKey) // "cell:{id}:users" 형태
                    .map(k -> k.getBytes(StandardCharsets.UTF_8))
                    .collect(Collectors.toList());

            for (byte[] key : keys) {
                conn.sMembers(key);
            }

            List<Object> rawResults = conn.closePipeline();

            return rawResults.stream()
                    .map(result -> (Set<byte[]>) result)
                    .filter(Objects::nonNull)
                    .flatMap(set -> set.stream()
                            .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                    )
                    .collect(Collectors.toList());
        }
    }

    @Override
    public boolean isCellIdNotInTargetArea(String cellId) {
        return locationJpaRepository.findById(cellId).isEmpty();
    }

    private String createUserIdKey(String encodedUserId) {
        return "user:" + encodedUserId;
    }

    private String createCellIdUsersKey(String cellId) {
        return "cell:" + cellId + ":users";
    }

    private String createCellIdExpiriesKey(String cellId) {
        return "cell:" + cellId + ":expiry";
    }

}
