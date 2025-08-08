package com.knu.ddip.location.infrastructure.init;

import com.knu.ddip.location.application.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeoJsonInitializer implements ApplicationRunner {

    private final LocationService locationService;

    @Override
    public void run(ApplicationArguments args) {
        locationService.loadAndSaveGeoJsonFeatures();
    }
}
