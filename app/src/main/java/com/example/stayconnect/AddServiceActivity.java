package com.example.stayconnect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.stayconnect.databinding.ActivityAddServiceBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddServiceActivity extends AppCompatActivity implements LocationListener {

    private ActivityAddServiceBinding binding;

    private static final String TAG = "ADD_SERVICE_TAG";

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private Uri imageUri = null;

    private ArrayList<ModelImagePicked> imagePickedArrayList;

    private AdapterImagesPicked adapterImagesPicked;









    LocationManager locationManager;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();


        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        imagePickedArrayList = new ArrayList<>();
        loadImages();

        binding.toolbarAddImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickOptions();
            }
        });


        binding.getLocationTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(AddServiceActivity.this, LocationPickerActivity.class);

                if(ContextCompat.checkSelfPermission(AddServiceActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(AddServiceActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                }

                getLocation();

            }
        });

        binding.addDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private ActivityResultLauncher<Intent> locationPickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");

                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Intent data = result.getData();

                        if (data != null) {

                            latitude = data.getDoubleExtra("latitude", 0.0);
                            longitude = data.getDoubleExtra("longitude", 0.0);
                            hostelAddress = data.getStringExtra("address");

                            Log.d(TAG, "onActivityResult: Latitude : " + latitude);
                            Log.d(TAG, "onActivityResult: Longitude : " + longitude);
                            Log.d(TAG, "onActivityResult: Address : " + hostelAddress);

                            binding.hostelAddress.setText(hostelAddress);

                        }else{
                            Log.d(TAG, "onActivityResult: Cancelled");
                            Toast.makeText(AddServiceActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    private void loadImages() {
        Log.d(TAG, "loadImages: ");

        adapterImagesPicked = new AdapterImagesPicked(this, imagePickedArrayList);
        binding.imagesRv.setAdapter(adapterImagesPicked);
    }

    private void showImagePickOptions() {

        Log.d(TAG, "showImagePickOptions: ");

        PopupMenu popupMenu = new PopupMenu(this, binding.toolbarAddImageBtn);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == 1) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                        requestCameraPermission.launch(new String[]{Manifest.permission.CAMERA});
                    }else{
                        requestCameraPermission.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }

                } else if (itemId == 2) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                        pickImageGallery();
                    } else {

                        requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }

                return false;
            }
        });

    }

    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted :"+isGranted);

                    if(isGranted){
                        pickImageGallery();
                    }else{
                        Toast.makeText(AddServiceActivity.this, "Storage Permission Denied...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<String[]> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: ");
                    Log.d(TAG, "onActivityResult: "+result.toString());

                    boolean areAllGranted = true;
                    for(boolean isGranted : result.values()){
                        areAllGranted  = areAllGranted && isGranted;
                    }
                    
                    if(areAllGranted){
                        pickImageCamera();
                    }else{
                        Toast.makeText(AddServiceActivity.this, "Camera or Storage or both Permissions denied...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickImageCamera() {

        Log.d(TAG, "pickImageCamera: ");

        ContentValues contentValues = new ContentValues();

        contentValues.put(MediaStore.Images.Media.TITLE, "TEMPORARY_IMAGE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMPORARY_IMAGE_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private void pickImageGallery() {

        Log.d(TAG, "pickImageGallery: ");

        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);

    }


    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if(result.getResultCode() == Activity.RESULT_OK){

                        Log.d(TAG, "onActivityResult: Image Picked : "+imageUri);

                        String timeStamp = ""+System.currentTimeMillis();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timeStamp, imageUri, null, false);
                        imagePickedArrayList.add(modelImagePicked);

                        loadImages();

                    }else{
                        Toast.makeText(AddServiceActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );


    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");

                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Intent data = result.getData();

                        imageUri = data.getData();

                        Log.d(TAG, "onActivityResult: Image Picked From Gallery: "+imageUri);

                        String timeStamp = ""+System.currentTimeMillis();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timeStamp, imageUri, null, false);
                        imagePickedArrayList.add(modelImagePicked);

                        loadImages();

                    }else{
                        Toast.makeText(AddServiceActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );


    private String noOfRooms = "";
    private String roomType = "";
    private String roomCapacity = "";
    private String rent = "";
    private String hostelName = "";
    private String hostelAddress = "";
    private String ownerContactNumber = "";
    private String descriptionEt = "";
    private double latitude = 0;
    private double longitude = 0;

    private void validateData() {

        noOfRooms = binding.noOfRooms.getText().toString().trim();
        roomType = binding.roomType.getText().toString().trim();
        roomCapacity = binding.roomCapacity.getText().toString().trim();
        rent = binding.rent.getText().toString().trim();
        hostelName = binding.hostelName.getText().toString().trim();
        hostelAddress = binding.hostelAddress.getText().toString().trim();
        ownerContactNumber = binding.ownerContactNumber.getText().toString().trim();
        descriptionEt = binding.descriptionEt.getText().toString().trim();

        if (noOfRooms.isEmpty()) {
            binding.noOfRooms.setError("Enter No. of Rooms");
            binding.noOfRooms.requestFocus();

        } else if (roomType.isEmpty()) {
            binding.roomType.setError("Enter No. of Rooms");
            binding.roomType.requestFocus();

        } else if (roomCapacity.isEmpty()) {
            binding.roomCapacity.setError("Enter No. of Rooms");
            binding.roomCapacity.requestFocus();

        } else if (rent.isEmpty()) {
            binding.rent.setError("Enter No. of Rooms");
            binding.rent.requestFocus();

        } else if (hostelName.isEmpty()) {
            binding.hostelName.setError("Enter No. of Rooms");
            binding.hostelName.requestFocus();

        } else if (hostelAddress.isEmpty()) {
            binding.hostelAddress.setError("Enter No. of Rooms");
            binding.hostelAddress.requestFocus();

        } else if (ownerContactNumber.isEmpty()) {
            binding.ownerContactNumber.setError("Enter No. of Rooms");
            binding.ownerContactNumber.requestFocus();

        } else if (descriptionEt.isEmpty()) {
            binding.descriptionEt.setError("Enter No. of Rooms");
            binding.descriptionEt.requestFocus();
        }else if(imagePickedArrayList.isEmpty()){
            Toast.makeText(this, "Pick At-Least one image", Toast.LENGTH_SHORT).show();
            
        }else{
            addService();
        }


    }

    private void addService() {

        Log.d(TAG, "addService: ");

        progressDialog.setMessage("Publishing Hosel");
        progressDialog.show();

        long timeStamp = System.currentTimeMillis();

        DatabaseReference refAds = FirebaseDatabase.getInstance().getReference("Ads");
        String keyId = refAds.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+keyId);
        hashMap.put("uid", ""+firebaseAuth.getUid());
        hashMap.put("noOfRooms", ""+noOfRooms);
        hashMap.put("roomType", ""+roomType);
        hashMap.put("roomCapacity", ""+roomCapacity);
        hashMap.put("rent", ""+rent);
        hashMap.put("hostelName", ""+hostelName);
        hashMap.put("hostelAddress", ""+hostelAddress);
        hashMap.put("ownerContactNumber", ""+ownerContactNumber);
        hashMap.put("description", ""+descriptionEt);
        hashMap.put("timeStamp", ""+timeStamp);
        hashMap.put("latitude", ""+latitude);
        hashMap.put("longitude", ""+longitude);

        refAds.child(keyId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d(TAG, "onSuccess: Hostel Published...");

                        uploadImageStorage(keyId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();

                        Toast.makeText(AddServiceActivity.this, "Failed to publish hostel due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImageStorage(String adId) {
        Log.d(TAG, "uploadImageStorage: ");

        for(int i=0; i<imagePickedArrayList.size(); i++){
            ModelImagePicked modelImagePicked = imagePickedArrayList.get(i);

            String imageName = modelImagePicked.getId();

            String filePathAndName = "And/"+imageName;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

            int imageIndexForProgress = i+1;

            storageReference.putFile(modelImagePicked.getImageUri())
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                            String message = "Uploading " + imageIndexForProgress + " of " + imagePickedArrayList.size() + " images...\nProgress " + (int)progress + "%";
                            
                            progressDialog.setMessage(message);
                            progressDialog.show();
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Log.d(TAG, "onSuccess: ");

                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());
                            Uri uploadedUrl = uriTask.getResult();

                            if (uriTask.isSuccessful()) {

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", ""+modelImagePicked.getId());
                                hashMap.put("imageUrl", ""+uploadedUrl);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
                                ref.child(adId).child("Images")
                                        .child(imageName)
                                        .updateChildren(hashMap);
                            }
                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: ", e);
                            
                            progressDialog.dismiss();
                        }
                    });
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Geocoder geocoder = new Geocoder(AddServiceActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);

            latitude = location.getLatitude();
            longitude = location.getLongitude();

            binding.hostelAddress.setText(address);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    @SuppressLint("MissingPermission")
    private void getLocation(){
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, AddServiceActivity.this);
    }
}