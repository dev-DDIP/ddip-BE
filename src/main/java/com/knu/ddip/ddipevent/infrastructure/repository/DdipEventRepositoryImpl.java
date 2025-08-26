package com.knu.ddip.ddipevent.infrastructure.repository;

import com.knu.ddip.ddipevent.application.service.DdipEventRepository;
import com.knu.ddip.ddipevent.domain.DdipEvent;
import com.knu.ddip.ddipevent.infrastructure.DdipMapper;
import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
@RequiredArgsConstructor
public class DdipEventRepositoryImpl implements DdipEventRepository {

    private final DdipEventJpaRepository ddipEventJpaRepository;
    private final DdipMapper ddipMapper;
//    private final JPAQueryFactory queryFactory;
//    private final RedisTemplate<String, String> redisTemplate;
//    private static final int S2_LEVEL = 15;

    @Override
    public DdipEvent save(DdipEvent ddipEvent) {
        DdipEventEntity entity = ddipMapper.toEntity(ddipEvent);
        DdipEventEntity savedEntity = ddipEventJpaRepository.save(entity);
        return ddipMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<DdipEvent> findById(UUID id) {
        return ddipEventJpaRepository.findById(id).map(ddipMapper::toDomain);
    }

    @Override
    public List<DdipEvent> findWithinBounds(double swLat, double swLon, double neLat, double neLon, String sort, Double userLat, Double userLon) {
        // TODO: 실제 S2를 이용한 로직 구현 필요. 현재는 전체 다 반환
        return ddipEventJpaRepository.findAll()
                .stream().map(ddipMapper::toDomain).toList();
    }
//        S2LatLng sw = S2LatLng.fromDegrees(swLat, swLon);
//        S2LatLng ne = S2LatLng.fromDegrees(neLat, neLon);
//        S2RegionCoverer coverer = new S2RegionCoverer();
//        coverer.setMinLevel(S2_LEVEL);
//        coverer.setMaxLevel(S2_LEVEL);
//        List<S2CellId> cellIds = coverer.getCovering(new com.google.common.geometry.S2LatLngRect(sw, ne)).cellIds();
//
//        List<String> keys = cellIds.stream()
//                .map(cellId -> "s2:" + cellId.toToken())
//                .toList();
//
//        List<String> eventIds = keys.stream()
//                .flatMap(key -> redisTemplate.opsForSet().members(key).stream())
//                .distinct()
//                .toList();
//
//        if (eventIds.isEmpty()) {
//            return List.of();
//        }
//
//        List<DdipEvent> events = queryFactory
//                .selectFrom(ddipEventEntity)
//                .where(ddipEventEntity.id.in(eventIds.stream().map(UUID::fromString).collect(Collectors.toList()))
//                        .and(ddipEventEntity.latitude.between(swLat, neLat))
//                        .and(ddipEventEntity.longitude.between(swLon, neLon)))
//                .fetch()
//                .stream()
//                .map(ddipMapper::toDomain)
//                .collect(Collectors.toList());
//
//        if ("distance".equals(sort) && userLat != null && userLon != null) {
//            events.sort((e1, e2) -> {
//                double dist1 = distance(userLat, userLon, e1.getLatitude(), e1.getLongitude());
//                double dist2 = distance(userLat, userLon, e2.getLatitude(), e2.getLongitude());
//                return Double.compare(dist1, dist2);
//            });
//        } else {
//            events.sort((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()));
//        }
//
//        return events;
//    }
//
//    private double distance(double lat1, double lon1, double lat2, double lon2) {
//        double theta = lon1 - lon2;
//        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
//        dist = Math.acos(dist);
//        dist = Math.toDegrees(dist);
//        dist = dist * 60 * 1.1515 * 1609.344;
//        return dist;
//    }
}
