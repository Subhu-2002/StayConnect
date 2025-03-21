package com.example.stayconnect;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GetLocation {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private final Context mContext;
    private LocationCallback mCallback;

    public GetLocation(Context context, LocationCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void checkPermission() {
        if (checkLocationPermission()) {
            getCurrentLocation();
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (mContext instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);

        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(mContext, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void getCurrentLocation() {
        if(checkLocationPermission()){
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            updateLocationTextView(location);
                        } else {
                            Toast.makeText(mContext, "Location not found", Toast.LENGTH_SHORT).show();
                            if(mCallback != null){
                                mCallback.onLocationReady(null, null);
                            }
                        }
                    });
        } else {
            Toast.makeText(mContext, "Location permission not available", Toast.LENGTH_SHORT).show();
        }

    }


    private void updateLocationTextView(Location location) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String cityName = address.getLocality() != null ? address.getLocality() : "Unknown City";
                String stateName = address.getAdminArea() != null ? address.getAdminArea() : "Unknown State";
                String areaName = address.getSubLocality() != null ? address.getSubLocality() : "Unkown Area";
                String fullAddress = areaName + " , " + cityName + " , " + stateName;
                if (mCallback != null) {
                    mCallback.onLocationReady(location, fullAddress);
                    mCallback.onLocationReady(location, location.getLatitude(), location.getLongitude());
                }
                // Now we notify the activity using a callback
            } else{
                if(mCallback!= null){
                    mCallback.onLocationReady(null, "Location not found");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(mCallback != null){
                mCallback.onLocationReady(null, "Error: Location not found");
            }
        }
    }
}