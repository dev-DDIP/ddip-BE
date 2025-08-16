package com.knu.ddip.location.infrastructure.repositoroy;

import com.knu.ddip.location.application.service.LocationReader;
import com.knu.ddip.location.application.util.LocationKeyFactory;
import com.knu.ddip.location.exception.LocationNotFoundException;
import com.knu.ddip.location.infrastructure.entity.LocationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class LocationReaderImpl implements LocationReader {

    private final LocationJpaRepository locationJpaRepository;
    private final RedisTemplate<String, String> redisTemplate;

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

        if (connectionFactory == null) return null; // throw xxx

        try (RedisConnection conn = connectionFactory.getConnection()) {
            conn.openPipeline();

            List<byte[]> keys = targetCellIds.stream()
                    .map(LocationKeyFactory::createCellIdUsersKey) // "cell:{id}:users" 형태
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
}
