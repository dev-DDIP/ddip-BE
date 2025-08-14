package com.knu.ddip.location.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository {
    void deleteAll();

    void saveAll(List<String> cellIds);

    void saveUserIdByCellId(String encodedUserId, String cellId);

    void validateLocationByCellId(String cellId);

    List<String> findAllLocationsByCellIdIn(List<String> cellIds);

    List<String> findUserIdsByCellIds(List<String> targetCellIds);

    boolean isCellIdNotInTargetArea(String cellId);

    void saveUserIdByCellIdAtomic(String newCellId, boolean cellIdNotInTargetArea, String encodedUserId);
}
