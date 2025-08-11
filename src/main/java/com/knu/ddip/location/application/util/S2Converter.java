package com.knu.ddip.location.application.util;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;

public abstract class S2Converter {

    public static S2CellId toCellId(double lat, double lng, int level) {
        S2LatLng latLng = S2LatLng.fromDegrees(lat, lng);
        return S2CellId.fromLatLng(latLng).parent(level);
    }
}
