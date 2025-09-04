package com.knu.ddip.ddipevent.infrastructure.repository;

import com.knu.ddip.ddipevent.application.service.DdipEventRepository;
import com.knu.ddip.ddipevent.domain.DdipEvent;
import com.knu.ddip.ddipevent.domain.DdipStatus;
import com.knu.ddip.ddipevent.infrastructure.DdipMapper;
import com.knu.ddip.ddipevent.infrastructure.entity.DdipEventEntity;
import com.knu.ddip.location.application.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Transactional(readOnly = true)
@Repository
@RequiredArgsConstructor
public class DdipEventRepositoryImpl implements DdipEventRepository {

    private final DdipEventJpaRepository ddipEventJpaRepository;
    private final DdipMapper ddipMapper;

    private final LocationService locationService;

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
        List<String> cellIds = locationService.getNeighborCellIdsToRetrieveNearDdipRequest(swLat, swLon, neLat, neLon);

        List<DdipEventEntity> ddipEventEntities = ddipEventJpaRepository.findAllByCellIdIn(cellIds);

        Comparator<DdipEventEntity> comparator = (o1, o2) -> {
            double dist1 = Math.pow(userLat - o1.getLatitude(), 2) + Math.pow(userLon - o1.getLongitude(), 2);
            double dist2 = Math.pow(userLat - o2.getLatitude(), 2) + Math.pow(userLon - o2.getLongitude(), 2);
            return dist1 - dist2 >= 0 ? 1 : -1;
        };

        // 유저와 이벤트 거리 비교해서 거리 가까운 순 정렬
        return ddipEventEntities.stream()
                .filter(event -> event.getStatus().equals(DdipStatus.OPEN))
                .sorted(comparator)
                .map(ddipMapper::toDomain)
                .toList();
    }
}
