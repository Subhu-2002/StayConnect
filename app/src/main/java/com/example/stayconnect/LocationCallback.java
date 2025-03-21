package com.example.stayconnect;

import android.location.Location;

public interface LocationCallback {
    void onLocationReady(Location location, String fullAddress);

    void onLocationReady(Location location, double latitude, double longitude);
}