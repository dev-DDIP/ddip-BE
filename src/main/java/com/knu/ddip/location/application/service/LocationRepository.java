package com.knu.ddip.location.application.service;

import java.util.List;

public interface LocationRepository {
    void deleteAll();

    void saveAll(List<String> cellIds);
}
