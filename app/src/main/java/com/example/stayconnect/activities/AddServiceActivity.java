package com.example.stayconnect.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.stayconnect.R;
import com.example.stayconnect.Utils;
import com.example.stayconnect.adapters.AdapterImagesPicked;
import com.example.stayconnect.databinding.ActivityAddServiceBinding;
import com.example.stayconnect.models.ModelImagePicked;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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

    private boolean isEditMode;

    private String adIdForEditing;

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


        ArrayAdapter<String> adapterCategories = new ArrayAdapter<>(this, R.layout.row_category_act, Utils.categories);
        binding.categoryAct.setAdapter(adapterCategories);


        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", false);
        Log.d(TAG, "onCreate: isEditMode: " + isEditMode);

        if (isEditMode) {

            adIdForEditing = intent.getStringExtra("adId");

            loadAdDetails();

            binding.toolbarTitleTv.setText("Update Ad");
            binding.postAdBtn.setText("Update Ad");
        } else {

            binding.toolbarTitleTv.setText("Create Ad");
            binding.postAdBtn.setText("Create Ad");
        }

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


        binding.locationAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddServiceActivity.this, LocationPickerActivity.class);
                locationPickerActivityResultLauncher.launch(intent);

            }
        });

        binding.postAdBtn.setOnClickListener(new View.OnClickListener() {
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

                            binding.locationAct.setText(hostelAddress);

                        } else {
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
                    } else {
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
                    Log.d(TAG, "onActivityResult: isGranted :" + isGranted);

                    if (isGranted) {
                        pickImageGallery();
                    } else {
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
                    Log.d(TAG, "onActivityResult: " + result.toString());

                    boolean areAllGranted = true;
                    for (boolean isGranted : result.values()) {
                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted) {
                        pickImageCamera();
                    } else {
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

                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Log.d(TAG, "onActivityResult: Image Picked : " + imageUri);

                        String timeStamp = "" + System.currentTimeMillis();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timeStamp, imageUri, null, false);
                        imagePickedArrayList.add(modelImagePicked);

                        loadImages();

                    } else {
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

                        Log.d(TAG, "onActivityResult: Image Picked From Gallery: " + imageUri);

                        String timeStamp = "" + System.currentTimeMillis();

                        ModelImagePicked modelImagePicked = new ModelImagePicked(timeStamp, imageUri, null, false);
                        imagePickedArrayList.add(modelImagePicked);

                        loadImages();

                    } else {
                        Toast.makeText(AddServiceActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );


    private String ownerName = "";
    private String category = "";
    private String rent = "";
    private String hostelName = "";
    private String hostelAddress = "";
    private String[] images;
    private String ownerContactNumber = "";
    private String descriptionEt = "";
    private double latitude = 0.0;
    private double longitude = 0.0;

    private void validateData() {

        ownerName = binding.ownerNameEt.getText().toString().trim();
        rent = binding.rentEt.getText().toString().trim();
        category = binding.categoryAct.getText().toString().trim();
        hostelName = binding.titleEt.getText().toString().trim();
        hostelAddress = binding.locationAct.getText().toString().trim();
        ownerContactNumber = binding.contactEt.getText().toString().trim();
        descriptionEt = binding.descriptionEt.getText().toString().trim();

        if (ownerName.isEmpty()) {
            binding.ownerNameEt.setError("Enter Owner Name");
            binding.ownerNameEt.requestFocus();

        } else if (category.isEmpty()) {
            binding.categoryAct.setError("Enter No. of Rooms");
            binding.categoryAct.requestFocus();

        } else if (rent.isEmpty()) {
            binding.rentEt.setError("Enter Price");
            binding.rentEt.requestFocus();

        } else if (hostelName.isEmpty()) {
            binding.titleEt.setError("Enter Mess/Hostel Name");
            binding.titleEt.requestFocus();

        } else if (hostelAddress.isEmpty()) {
            binding.locationAct.setError("Click Here For Location");
            binding.locationAct.requestFocus();

        } else if (ownerContactNumber.isEmpty()) {
            binding.contactEt.setError("Enter Mobile Number");
            binding.contactEt.requestFocus();

        } else if (descriptionEt.isEmpty()) {
            binding.descriptionEt.setError("Enter description");
            binding.descriptionEt.requestFocus();
        } else if (imagePickedArrayList.isEmpty()) {
            Toast.makeText(this, "Pick At-Least one image", Toast.LENGTH_SHORT).show();

        } else {
            if (isEditMode) {
                updateAd();
            } else {
                addService();
            }
        }


    }

    private void updateAd() {

        Log.d(TAG, "updateAd: ");

        progressDialog.setMessage("Updating Ad...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("category", "" + category);
        hashMap.put("rent", "" + rent);
        hashMap.put("adName", "" + hostelName);
        hashMap.put("adAddress", "" + hostelAddress);
        hashMap.put("ownerContactNumber", "" + ownerContactNumber);
        hashMap.put("ownerName", "" + ownerName);
        hashMap.put("description", "" + descriptionEt);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adIdForEditing)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        Toast.makeText(AddServiceActivity.this, "Failed to update Ad due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addService() {

        Log.d(TAG, "addService: ");

        progressDialog.setMessage("Publishing Hostel");
        progressDialog.show();

        long timeStamp = System.currentTimeMillis();

        DatabaseReference refAds = FirebaseDatabase.getInstance().getReference("Ads");
        String keyId = refAds.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + keyId);
        hashMap.put("uid", "" + firebaseAuth.getUid());
        hashMap.put("category", "" + category);
        hashMap.put("ownerName", "" + ownerName);
        hashMap.put("rent", "" + rent);
        hashMap.put("hostelName", "" + hostelName);
        hashMap.put("hostelAddress", "" + hostelAddress);
        hashMap.put("ownerContactNumber", "" + ownerContactNumber);
        hashMap.put("description", "" + descriptionEt);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);

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

                        Toast.makeText(AddServiceActivity.this, "Failed to publish hostel due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImageStorage(String adId) {
        Log.d(TAG, "uploadImageStorage: ");

        for (int i = 0; i < imagePickedArrayList.size(); i++) {
            ModelImagePicked modelImagePicked = imagePickedArrayList.get(i);    

            String imageName = modelImagePicked.getId();

//            String filePathAndName = "And/" + imageName;

//            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

            int imageIndexForProgress = i + 1;

            MediaManager.get().upload(modelImagePicked.getImageUri())
                    .unsigned("android_profile_upload")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {

                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {

                            double progress = (100.0 * bytes) / totalBytes;
                            String message = "Uploading " + imageIndexForProgress + " of " + imagePickedArrayList.size() + " image...\nProgress " + (int) progress + "%";
                            progressDialog.setMessage(message);
                            progressDialog.show();

                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {

                            String publicId = (String) resultData.get("public_id");
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference userRef = database.getReference("Ads")
                                    .child(adId).child("Images");

                            userRef.child("servicePicture"+imageIndexForProgress).setValue(publicId);

                            progressDialog.dismiss();

                            Log.d(TAG, "onSuccess: ");
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {

                            progressDialog.dismiss();
                            Toast.makeText(AddServiceActivity.this, "Failed to upload images", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            progressDialog.dismiss();
                            Toast.makeText(AddServiceActivity.this, "Failed to upload images", Toast.LENGTH_SHORT).show();
                        }
                    }).dispatch();



//            storageReference.putFile(modelImagePicked.getImageUri())
//                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//                            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
//
//                            String message = "Uploading " + imageIndexForProgress + " of " + imagePickedArrayList.size() + " images...\nProgress " + (int) progress + "%";
//
//                            progressDialog.setMessage(message);
//                            progressDialog.show();
//                        }
//                    })
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                            Log.d(TAG, "onSuccess: ");
//
//                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
//                            while (!uriTask.isSuccessful()) ;
//                            Uri uploadedUrl = uriTask.getResult();
//
//                            if (uriTask.isSuccessful()) {
//
//                                HashMap<String, Object> hashMap = new HashMap<>();
//                                hashMap.put("id", "" + modelImagePicked.getId());
//                                hashMap.put("imageUrl", "" + uploadedUrl);
//
//                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
//                                ref.child(adId).child("Images")
//                                        .child(imageName)
//                                        .updateChildren(hashMap);
//                            }
//                            progressDialog.dismiss();
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.e(TAG, "onFailure: ", e);
//
//                            progressDialog.dismiss();
//                        }
//                    });
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

            binding.locationAct.setText(address);
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
    private void getLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, AddServiceActivity.this);
    }

    private void loadAdDetails() {
        Log.d(TAG, "loadAdDetails: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adIdForEditing)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String rent = "" + snapshot.child("rent").getValue();
                        String hostelName = "" + snapshot.child("hostelName").getValue();
                        String hostelAddress = "" + snapshot.child("hostelAddress").getValue();
                        String ownerContactNumber = "" + snapshot.child("ownerContactNumber").getValue();
                        String ownerName = "" + snapshot.child("ownerName").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String category = "" + snapshot.child("category").getValue();
                        String latitude = "" + snapshot.child("latitude").getValue();
                        String longitude = "" + snapshot.child("longitude").getValue();

                        binding.ownerNameEt.setText(ownerName);
                        binding.rentEt.setText(rent);
                        binding.titleEt.setText(hostelName);
                        binding.locationAct.setText(hostelAddress);
                        binding.contactEt.setText(ownerContactNumber);
                        binding.descriptionEt.setText(description);
                        binding.categoryAct.setText(category);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}