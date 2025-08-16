package com.knu.ddip.location.application.service;

import java.util.List;

public interface LocationReader {
    void validateLocationByCellId(String cellId);

    List<String> findAllLocationsByCellIdIn(List<String> cellIds);

    List<String> findUserIdsByCellIds(List<String> targetCellIds);

    boolean isCellIdNotInTargetArea(String cellId);
}
