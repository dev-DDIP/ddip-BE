package com.knu.ddip.location.application.init;

import com.knu.ddip.location.application.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeoJsonInitializer implements ApplicationRunner {

    public static final String GEOJSON_INIT_LOCK_KEY = "lock:geojson:init";

    private final OneTimeRunner oneTimeRunner;
    private final LocationService locationService;

    @Override
    public void run(ApplicationArguments args) {
        oneTimeRunner.runOnce(
                GEOJSON_INIT_LOCK_KEY,
                () -> locationService.loadAndSaveGeoJsonFeatures()
        );
    }
}
