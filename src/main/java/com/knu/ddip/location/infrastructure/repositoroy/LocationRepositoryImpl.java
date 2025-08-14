package com.knu.ddip.location.infrastructure.repositoroy;

import com.knu.ddip.location.application.service.LocationRepository;
import com.knu.ddip.location.exception.LocationNotFoundException;
import com.knu.ddip.location.infrastructure.entity.LocationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
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
    public Optional<String> findCellIdByUserId(String encodedUserId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(createUserIdKey(encodedUserId)));
    }

    @Override
    public void deleteUserIdByCellId(String encodedUserId, String cellId) {
        redisTemplate.opsForSet().remove(createCellIdUsersKey(cellId), encodedUserId);
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
    public void saveCellIdByUserId(String encodedUserId, String cellId) {
        redisTemplate.opsForValue().set(createUserIdKey(encodedUserId), cellId, TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void validateLocationByCellId(String cellId) {
        locationJpaRepository.findById(cellId)
                .orElseThrow(() -> new LocationNotFoundException("위치를 찾을 수 없습니다."));
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

        if (connectionFactory == null) return null;

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
