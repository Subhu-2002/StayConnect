package com.example.stayconnect.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stayconnect.R;
import com.example.stayconnect.databinding.ActivityLocationPickerBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityLocationPickerBinding binding;

    private static final String TAG = "LOCATION_PICKER_TAG";

    private static final int DEFAULT_ZOOM = 15;

    private GoogleMap mMap = null;

    private PlacesClient mPlacesClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private Location mLastKnownLocation = null;
    private Double selectedLatitude = null;
    private Double selectedLongitude = null;
    private String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLocationPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        Places.initialize(this, getString(R.string.google_api_key));

        mPlacesClient = Places.createClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autoComplete_fragment);

        Place.Field[] placesList = new Place.Field[]{Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG};

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(placesList));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {

                String id = place.getId();
                String title = place.getName();
                LatLng latLng = place.getLatLng();
                selectedLatitude = latLng.latitude;
                selectedLongitude = latLng.longitude;
                selectedAddress = place.getAddress();

                Log.d(TAG, "onPlaceSelected: Id :"+id);
                Log.d(TAG, "onPlaceSelected: title :"+title);
                Log.d(TAG, "onPlaceSelected: selectedLatitude :"+selectedLatitude);
                Log.d(TAG, "onPlaceSelected: selectedLongitude :"+selectedLongitude);
                Log.d(TAG, "onPlaceSelected: selectedAddress :"+selectedAddress);

                addMarker(latLng, title, selectedAddress);
            }

            @Override
            public void onError(@NonNull Status status) {


            }
        });

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.toolbarLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGPSEnabled()) {

                    requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }else{
                    Toast.makeText(LocationPickerActivity.this, "Location is not on! Turn it on to show current location...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("latitude", selectedLatitude);
                intent.putExtra("longitude", selectedLongitude);
                intent.putExtra("address", selectedAddress);
                setResult(RESULT_OK, intent);

                finish();
            }
        });

    }

    private void addMarker(LatLng latLng, String title, String address) {

        Log.d(TAG, "addMarker: latitude :"+latLng.latitude);
        Log.d(TAG, "addMarker: longitude :"+latLng.longitude);
        Log.d(TAG, "addMarker: title :"+title);
        Log.d(TAG, "addMarker: address :"+address);

        mMap.clear();

        try {

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(""+title);
            markerOptions.snippet(""+address);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

            binding.doneBtn.setVisibility(View.VISIBLE);
            binding.selectedPlaceTv.setText(address);

        }catch (Exception e){
            Log.e(TAG, "addMarker: ", e);
        }
    }


    private boolean isGPSEnabled(){
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        }catch (Exception e){
            Log.e(TAG, "isGPSEnabled: ", e);
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, "isGPSEnabled: ", e);
        }

        return !(!gpsEnabled && !networkEnabled);
    }

    private void detectAndShowDeviceLocationMap(){

        try {

            @SuppressLint("MissingPermission") Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            if (location != null) {
                                mLastKnownLocation = location;

                                selectedLatitude = location.getLatitude();
                                selectedLongitude = location.getLongitude();

                                Log.d(TAG, "onSuccess: Selected Latitude :"+selectedLatitude);
                                Log.d(TAG, "onSuccess: Selected Longitude :"+selectedLongitude);

                                LatLng latLng = new LatLng(selectedLatitude, selectedLongitude);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));


                                addressFromLatlng(latLng);
                            }else{
                                Log.d(TAG, "onSuccess: Location is null");
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: ", e);
                        }
                    });

        }catch (Exception e){
            Log.e(TAG, "detectAndShowDeviceLocationMap: ", e);
        }
    }


    private void pickCurrentPlace(){
        Log.d(TAG, "pickCurrentPlace: ");
        if (mMap == null) {
            return;
        }

        detectAndShowDeviceLocationMap();
    }


    private void addressFromLatlng(LatLng latLng){
        Log.d(TAG, "addressFromLatlng: ");

        Geocoder geocoder = new Geocoder(this);
        try{
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            Address address = addressList.get(0);

            String addressLine = address.getAddressLine(0);
            String countryName = address.getCountryName();
            String adminArea = address.getAdminArea();
            String subAdminArea = address.getSubAdminArea();
            String locality = address.getLocality();
            String subLocality = address.getSubLocality();
            String postalCode = address.getPostalCode();

            selectedAddress = ""+addressLine;

            addMarker(latLng, ""+subLocality, ""+addressLine);

        }catch (Exception e){
            Log.e(TAG, "addressFromLatlng: ", e);
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        Log.d(TAG, "onMapReady: ");

        mMap = googleMap;

        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                selectedLatitude = latLng.latitude;
                selectedLongitude = latLng.longitude;

                Log.d(TAG, "onMapClick: selectedLatitude : "+selectedLatitude);
                Log.d(TAG, "onMapClick: selectedLongitude : "+selectedLongitude);

                addressFromLatlng(latLng);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private ActivityResultLauncher<String> requestLocationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {

                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: ");

                    if (isGranted) {

                        mMap.setMyLocationEnabled(true);
                        pickCurrentPlace();
                    }else{
                        Toast.makeText(LocationPickerActivity.this, "Permission denied...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

}