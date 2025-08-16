package com.knu.ddip.location.application.service;

import java.util.List;

public interface LocationWriter {
    void deleteAll();

    void saveAll(List<String> cellIds);

    void saveUserIdByCellIdAtomic(String newCellId, boolean cellIdNotInTargetArea, String encodedUserId);

    void cleanupExpiredUserLocations(long now);
}
