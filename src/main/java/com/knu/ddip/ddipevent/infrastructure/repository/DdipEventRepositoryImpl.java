package com.knu.ddip.ddipevent.infrastructure.repository;

import com.knu.ddip.ddipevent.application.service.DdipEventRepository;
import com.knu.ddip.ddipevent.domain.DdipEvent;
import com.knu.ddip.ddipevent.infrastructure.DdipMapper;
import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;
import com.knu.ddip.ddipevent.application.util.DistanceConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Transactional(readOnly = true)
@Repository
@RequiredArgsConstructor
public class DdipEventRepositoryImpl implements DdipEventRepository {

    private final DdipEventJpaRepository ddipEventJpaRepository;
    private final DdipMapper ddipMapper;
    private final DistanceConverter distanceConverter;

    @Transactional
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
        double dist = boundingBoxRadiusMeters(swLat, swLon, neLat, neLon, userLat, userLon);

        return ddipEventJpaRepository.findAllByDistance(userLon, userLat, dist).stream()
                .map(ddipMapper::toDomain)
                .toList();
    }

    private double boundingBoxRadiusMeters(double swLat, double swLon, double neLat, double neLon, Double userLat, Double userLon) {
        double[][] locations = new double[][]{
                {swLat, swLon},
                {neLat, neLon},

                {neLat, swLon},
                {swLat, neLon},

                {neLat, userLon},
                {swLat, userLon},

                {userLat, neLon},
                {userLat, swLon},
        };

        double maxDist = -1;
        for (double[] location : locations) {
            double dist = distanceConverter.haversineMeters(userLat, userLon, location[0], location[1]);
            maxDist = Math.max(maxDist, dist);
        }
        return maxDist;
    }
}
