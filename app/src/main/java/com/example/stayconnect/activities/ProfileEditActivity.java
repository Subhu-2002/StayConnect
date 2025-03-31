package com.example.stayconnect.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.stayconnect.R;
import com.example.stayconnect.databinding.ActivityProfileEditBinding;
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

import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {

    private ActivityProfileEditBinding binding;

    private static final String TAG = "PROFILE_EDIT_TAG";

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private Uri imageUri = null;

    private String userType = "";

    private String publicId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.profileImagePickFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String name = "";
    private String dob = "";
    private String email = "";
    private String phoneCode = "";
    private String phoneNumber = "";

    private void validateData() {

        name = binding.nameEt.getText().toString().trim();
        dob = binding.dobEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        phoneCode = binding.countryCodePicker.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();


        if (name.isEmpty()) {
            binding.nameEt.setError("Enter Your Name");
            binding.nameEt.requestFocus();
        } else if (email.isEmpty()) {
            binding.emailEt.setError("Enter Your Email");
            binding.emailEt.requestFocus();
        } else if (phoneNumber.isEmpty()) {
            binding.phoneNumberEt.setError("Enter Phone Number");
            binding.phoneNumberEt.requestFocus();
        } else{
            if (imageUri == null) {

                updateProfileDb(null);
            }else{

                uploadProfileImageStorage();
            }
        }
    }

    private void uploadProfileImageStorage(){

        Log.d(TAG, "uploadProfileImageStorage: ");

        progressDialog.setMessage("Uploading user profile image...");
        progressDialog.show();




        MediaManager.get().upload(imageUri)
                .unsigned("android_profile_upload")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {

                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {

                        double progress = (100.0 * bytes) / totalBytes;
                        String message = "Uploading " + " image...\nProgress " + (int) progress + "%";
                        progressDialog.setMessage(message);

                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        publicId = (String) resultData.get("public_id");

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference userRef = database.getReference("Users").child(firebaseAuth.getUid());

                        userRef.child("profilePicture").setValue(publicId);
                        progressDialog.dismiss();

                        startActivity(new Intent(ProfileEditActivity.this, MainActivity.class));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {

                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {

                    }
                }).dispatch();


        updateProfileDb(null);


//        String filePathAndName = "UserImages/" + "Profile_" + firebaseAuth.getUid();
//
//        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
//        ref.putFile(imageUri)
//                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
//                        Log.d(TAG, "onProgress: Progress: "+progress);
//
//                        progressDialog.setMessage("Uploading profile image. Progress: "+(int)progress + "%");
//                    }
//                })
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Log.d(TAG, "onSuccess: Uploaded");
//
//                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
//
//                        while (!uriTask.isSuccessful());
//                        String uploadedImageUri = uriTask.getResult().toString();
//
//                        if (uriTask.isSuccessful()) {
//                            updateProfileDb(uploadedImageUri);
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                        Log.e(TAG, "onFailure: ", e);
//                        progressDialog.dismiss();
//                        Toast.makeText(ProfileEditActivity.this, "Failed to upload profile image due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
    }

    private void updateProfileDb(String imageUrl){

        progressDialog.setMessage("Updating info...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", ""+name);
        hashMap.put("dob", ""+dob);
        
        if (userType.equalsIgnoreCase("Phone")) {
            hashMap.put("email", ""+email);
        }else if(userType.equalsIgnoreCase("Email") || userType.equalsIgnoreCase("Gmail")){
            hashMap.put("phoneCode", ""+phoneCode);
            hashMap.put("phoneNumber", ""+phoneNumber);
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Info updated...");
                        progressDialog.dismiss();
                        
                        Toast.makeText(ProfileEditActivity.this, "Profile Updated...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ProfileEditActivity.this, MainActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();

                        Toast.makeText(ProfileEditActivity.this, "Failed to update due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadMyInfo() {

        Log.d(TAG, "loadMyInfo: ");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String dob = "" + snapshot.child("dob").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String publicId = ""+ snapshot.child("profilePicture").getValue();
                        String imageUrl = MediaManager.get().url().generate(publicId);
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        userType = "" + snapshot.child("userType").getValue();
                        String name = "" + snapshot.child("name").getValue();

                        String phone = phoneCode + phoneNumber;

                        if (userType.equalsIgnoreCase("Email") || userType.equalsIgnoreCase("Google")) {

                            binding.emailEt.setEnabled(false);
                            binding.emailTil.setEnabled(false);
                        } else {

                            binding.phoneNumberTil.setEnabled(false);
                            binding.phoneNumberEt.setEnabled(false);
                            binding.countryCodePicker.setEnabled(false);
                        }

                        binding.emailEt.setText(email);
                        binding.dobEt.setText(dob);
                        binding.nameEt.setText(name);
                        binding.phoneNumberEt.setText(phoneNumber);

                        try {
                            binding.phoneNumberEt.setText(phoneNumber);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }

                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_person_gray)
                                    .into(binding.profileIv);

                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void imagePickDialog() {

        PopupMenu popupMenu = new PopupMenu(this, binding.profileImagePickFab);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");

         popupMenu.show();

         popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 
                 int itemId = item.getItemId();

                 if (itemId == 1) {

                     Log.d(TAG, "onMenuItemClick: Camera Clicked, check if camera permission(s) granted or not");
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                         requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA});

                     }else{

                         requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});

                     }
                 } else if (itemId == 2) {

                     Log.d(TAG, "onMenuItemClick: Check if storage permission is granted or not");

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

    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: "+result.toString());

                    boolean areAllGranted = true;
                    for(boolean isGranted: result.values()){
                        
                        areAllGranted = areAllGranted && isGranted;
                        
                    }
                    if (areAllGranted) {
                        Log.d(TAG, "onActivityResult: All Granted");
                        pickImageCamera();
                    }else{
                        Log.d(TAG, "onActivityResult: All or either one is denied");

                        Toast.makeText(ProfileEditActivity.this, "Camera or Storage or both permissions are denied...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG, "onActivityResult: isGranted: "+isGranted);

                    if (isGranted) {

                        pickImageGallery();
                    }else{

                        Toast.makeText(ProfileEditActivity.this, "Storage Permission Denied...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if(result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: Image Captured: "+imageUri);

                        try{
                            Glide.with(ProfileEditActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.ic_profile_white)
                                    .into(binding.profileIv);
                        }catch (Exception e){
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    }else{
                        Toast.makeText(ProfileEditActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );

    private void pickImageGallery(){

        Log.d(TAG, "pickImageGallery: ");

        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Intent data = result.getData();

                        imageUri = data.getData();

                        Log.d(TAG, "onActivityResult: Image Picked From Gallery: "+imageUri);


                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.ic_profile_white)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onActivityResult: ", e);
                        }
                    }else{
                        Toast.makeText(ProfileEditActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );
}