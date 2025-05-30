package com.example.stayconnect.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.stayconnect.R;
import com.example.stayconnect.RvListenerCategory;
import com.example.stayconnect.Utils;
import com.example.stayconnect.activities.LocationPickerActivity;
import com.example.stayconnect.adapters.AdapterCategory;
import com.example.stayconnect.adapters.AdapterAd;
import com.example.stayconnect.databinding.FragmentHomeBinding;
import com.example.stayconnect.models.ModelAd;
import com.example.stayconnect.models.ModelCategory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private static final String TAG = "HOME_TAG";

    private static final int MAX_DISTANCE_TO_LOAD_ADS_KM = 10;

    private Context mcontext;

    private ArrayList<ModelAd> hostelArrayList;

    private AdapterAd adapterAd;

    private SharedPreferences locationSp;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentAddress = "";


    @Override
    public void onAttach(@NonNull Context context) {
        mcontext = context;
        super.onAttach(context);
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        binding = FragmentHomeBinding.inflate(LayoutInflater.from(mcontext), container, false);
        return binding.getRoot();


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationSp = mcontext.getSharedPreferences("LOCATION_SP", Context.MODE_PRIVATE);
        currentLatitude = locationSp.getFloat("CURRENT_LATITUDE", 0.0f);
        currentLongitude = locationSp.getFloat("CURRENT_LONGITUDE", 0.0f);
        currentAddress = locationSp.getString("CURRENT_ADDRESS", "");
//
        if (currentLatitude != 0.0 && currentLongitude != 0.0) {
            binding.locationTv.setText(currentAddress);
        }

        loadCategories();

        loadAds("All");

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: Query : " + s);

                try {
                    String query = s.toString();

                    adapterAd.getFilter().filter(query);

                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.locationCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mcontext, LocationPickerActivity.class);
                locationPickerActivityResult.launch(intent);
            }
        });
    }

    private ActivityResultLauncher<Intent> locationPickerActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            if (result.getResultCode() == Activity.RESULT_OK) {
                Log.d(TAG, "onActivityResult: Result Ok");

                Intent data = result.getData();

                if (data != null) {

                    Log.d(TAG, "onActivityResult: Location Picked");

                    currentLatitude = data.getDoubleExtra("latitude", 0.0);
                    currentLongitude = data.getDoubleExtra("longitude", 0.0);
                    currentAddress = data.getStringExtra("address");

                    locationSp.edit().putFloat("CURRENT_LATITUDE", Float.parseFloat("" + currentLatitude)).putFloat("CURRENT_LONGITUDE", Float.parseFloat("" + currentLongitude)).putString("CURRENT_ADDRESS", currentAddress).apply();

                    binding.locationTv.setText(currentAddress);


                }
            } else {
                Log.d(TAG, "onActivityResult: Cancelled...");
                Toast.makeText(mcontext, "Cancelled...", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private void loadCategories() {
        ArrayList<ModelCategory> categoryArrayList = new ArrayList<>();

        ModelCategory modelCategoryAll = new ModelCategory("All", R.drawable.ic_category_all);
        categoryArrayList.add(modelCategoryAll);

        Log.d(TAG, "loadCategories: Utils.categories.length = " + Utils.categories.length);

        for (int i = 0; i < Utils.categories.length; i++) {
            ModelCategory modelCategory = new ModelCategory(Utils.categories[i], Utils.categoryIcons[i]);
            categoryArrayList.add(modelCategory);
        }

        AdapterCategory adapterCategory = new AdapterCategory(mcontext, categoryArrayList, new RvListenerCategory() {
            @Override
            public void onCategoryClick(ModelCategory modelCategory) {
                loadAds(modelCategory.getCategory());
            }
        });

        binding.serviceCategoryRv.setAdapter(adapterCategory);
    }

    private void loadAds(String selectedCategory) {
        Log.d(TAG, "loadAds: Category : " + selectedCategory);

        hostelArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads"); // Correct path based on image
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                hostelArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    ModelAd modelAd = ds.getValue(ModelAd.class);

                    double distance = calculateDistanceKm(modelAd.getLatitude(), modelAd.getLongitude());

                    if (selectedCategory.equals("All")) {
                        if (distance <= MAX_DISTANCE_TO_LOAD_ADS_KM) {

                            hostelArrayList.add(modelAd);
                        }
                    } else {
                        if (modelAd.getCategory().equals(selectedCategory)) {
                            hostelArrayList.add(modelAd);
                        }
                    }

                }
                adapterAd = new AdapterAd(mcontext, hostelArrayList);
                binding.servicesRv.setAdapter(adapterAd);
                adapterAd.notifyDataSetChanged(); // Notify adapter of changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: Database Error: " + error.getMessage());
                Toast.makeText(mcontext, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calculateDistanceKm(double hostelLatitude, double hostelLongitude) {

        Log.d(TAG, "calculateDistanceKm: latitude: " + currentLatitude);
        Log.d(TAG, "calculateDistanceKm: longitude: " + currentLongitude);
        Log.d(TAG, "calculateDistanceKm: hostelLatitude: " + hostelLatitude);
        Log.d(TAG, "calculateDistanceKm: hostelLongitude: " + hostelLongitude);

        Location startPoint = new Location(LocationManager.NETWORK_PROVIDER);
        startPoint.setLatitude(currentLatitude);
        startPoint.setLongitude(currentLongitude);

        Location endPoint = new Location(LocationManager.NETWORK_PROVIDER);
        endPoint.setLatitude(hostelLatitude);
        endPoint.setLongitude(hostelLongitude);

        double distanceInMeters = startPoint.distanceTo(endPoint);
        double distanceInKm = distanceInMeters / 1000;

        return distanceInKm;
    }
}