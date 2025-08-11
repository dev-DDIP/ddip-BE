package com.knu.ddip.location.application.service;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {
    void deleteAll();

    void saveAll(List<String> cellIds);

    Optional<String> findCellIdByUserId(String encodedUserId);

    void deleteUserIdByCellId(String encodedUserId, String cellIdByUserId);

    void saveUserIdByCellId(String encodedUserId, String cellId);

    void saveCellIdByUserId(String encodedUserId, String cellId);

    void validateLocationByCellId(String cellId);

    List<String> findAllLocationsByCellIdIn(List<String> cellIds);

    List<String> findUserIdsByCellId(String targetCellId);
}
