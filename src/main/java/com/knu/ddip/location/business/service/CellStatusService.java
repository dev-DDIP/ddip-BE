package com.knu.ddip.location.business.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CellStatusService {

    private final LocationReader locationReader;

    public int getAllCurrentUsersCount() {
        int sumCount = 0;
        List<String> cellIds = locationReader.findAllCellIds();
        for (String cellId : cellIds) {
            int count = locationReader.getUsersCountByCellId(cellId);
            sumCount += count;
        }
        return sumCount;
    }

    public int getCurrentUsersCountByCellId(String cellId) {
        return locationReader.getUsersCountByCellId(cellId);
    }

}
