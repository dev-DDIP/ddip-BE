package com.knu.ddip.location.presentation.controller;

import com.knu.ddip.location.application.dto.UpdateMyLocationRequest;
import com.knu.ddip.location.application.service.LocationService;
import com.knu.ddip.location.presentation.api.LocationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LocationController implements LocationApi {

    private final LocationService locationService;

    @Override
    public ResponseEntity<Void> updateMyLocation(UpdateMyLocationRequest request) {
        locationService.saveUserLocation(request.userId(), request);
        return ResponseEntity.ok().build();
    }
}
