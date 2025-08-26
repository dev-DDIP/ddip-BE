package com.knu.ddip.ddipevent.application.service;

import com.knu.ddip.ddipevent.domain.DdipEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DdipEventRepository {
    DdipEvent save(DdipEvent ddipEvent);

    Optional<DdipEvent> findById(UUID id);

    List<DdipEvent> findWithinBounds(double swLat, double swLon, double neLat, double neLon, String sort, Double userLat, Double userLon);
}
