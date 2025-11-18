package com.knu.ddip.location.application.util;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import org.springframework.stereotype.Component;

import static com.knu.ddip.location.application.util.S2Constants.LEVEL;

@Component
public class S2Converter {
    public S2CellId toCellId(double lat, double lng) {
        S2LatLng latLng = S2LatLng.fromDegrees(lat, lng);
        return S2CellId.fromLatLng(latLng).parent(LEVEL);
    }

    public String toCellIdString(double lat, double lng) {
        S2CellId cellId = toCellId(lat, lng);
        return cellId.toToken();
    }
}
