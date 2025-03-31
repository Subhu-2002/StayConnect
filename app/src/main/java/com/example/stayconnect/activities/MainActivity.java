package com.example.stayconnect.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.stayconnect.R;
import com.example.stayconnect.databinding.ActivityMainBinding;
import com.example.stayconnect.fragments.ChatsFragment;
import com.example.stayconnect.fragments.FavFragment;
import com.example.stayconnect.fragments.HomeFragment;
import com.example.stayconnect.fragments.ProfileFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private FirebaseAuth firebaseAuth;

    public static final String TAG = "MAIN_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            startLoginOptions();
        } else {
            updateFCMToken();
            askNotificationPermission();
        }

        showHomeFragment();


        binding.bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();
                if (itemId == R.id.menu_home) {

                    showHomeFragment();

                    return true;
                } else if (itemId == R.id.menu_chats) {

                    if (firebaseAuth.getCurrentUser() == null) {
                        Toast.makeText(MainActivity.this, "Login Required...", Toast.LENGTH_SHORT).show();
                        startLoginOptions();

                        return false;
                    } else {
                        showChatsFragment();

                        return true;
                    }
                } else if (itemId == R.id.menu_fav) {


                    if (firebaseAuth.getCurrentUser() == null) {
                        Toast.makeText(MainActivity.this, "Login Required...", Toast.LENGTH_SHORT).show();
                        startLoginOptions();

                        return false;
                    } else {
                        showFavFragment();

                        return true;
                    }
                } else if (itemId == R.id.menu_profile) {


                    if (firebaseAuth.getCurrentUser() == null) {
                        Toast.makeText(MainActivity.this, "Login Required...", Toast.LENGTH_SHORT).show();
                        startLoginOptions();

                        return false;
                    } else {
                        showProfileFragment();

                        return true;
                    }
                } else {

                    return false;
                }
            }
        });

        binding.addServiceActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddServiceActivity.class);
                intent.putExtra("isEditMode", false);
                startActivity(intent);
            }
        });

    }

    private void startLoginOptions() {
        startActivity(new Intent(MainActivity.this, LoginOptionActivity.class));
    }

    private void showProfileFragment() {
//        binding.toolbarTitleTv.setText("Profile");

        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "Profile Fragment");
        fragmentTransaction.commit();
    }

    private void showFavFragment() {
//        binding.toolbarTitleTv.setText("Fav");

        FavFragment fragment = new FavFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "Fav Fragment");
        fragmentTransaction.commit();
    }

    private void showChatsFragment() {
//        binding.toolbarTitleTv.setText("Chats");

        ChatsFragment fragment = new ChatsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "Chats Fragment");
        fragmentTransaction.commit();
    }

    private void showHomeFragment() {
//        binding.toolbarTitleTv.setText("Home");

        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "Home Fragment");
        fragmentTransaction.commit();
    }


    private void updateFCMToken() {

        String myUid = "" + firebaseAuth.getUid();
        Log.d(TAG, "updateFCMToken: myUid: " + myUid);

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String token) {
                Log.d(TAG, "onSuccess: token: " + token);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("fcmToken", token);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(myUid).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Token updated...");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: ", e);
            }
        });
    }

    private void askNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private ActivityResultLauncher<String> requestNotificationPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean isGranted) {
            Log.d(TAG, "onActivityResult: Notification Permission State : " + isGranted);
        }
    });
}