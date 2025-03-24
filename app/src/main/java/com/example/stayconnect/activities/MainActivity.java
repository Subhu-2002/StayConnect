package com.example.stayconnect.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.stayconnect.R;
import com.example.stayconnect.databinding.ActivityMainBinding;
import com.example.stayconnect.fragments.ChatsFragment;
import com.example.stayconnect.fragments.FavFragment;
import com.example.stayconnect.fragments.HomeFragment;
import com.example.stayconnect.fragments.ProfileFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null){
            startLoginOptions();
        }

        showHomeFragment();


        binding.bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();
                if(itemId == R.id.menu_home){
                    
                    showHomeFragment();

                    return true;
                }else if(itemId == R.id.menu_chats){

                    if(firebaseAuth.getCurrentUser() == null){
                        Toast.makeText(MainActivity.this, "Login Required...", Toast.LENGTH_SHORT).show();
                        startLoginOptions();

                        return false; 
                    }else{
                        showChatsFragment();

                        return true;
                    }
                }else if(itemId == R.id.menu_fav){


                    if(firebaseAuth.getCurrentUser() == null){
                        Toast.makeText(MainActivity.this, "Login Required...", Toast.LENGTH_SHORT).show();
                        startLoginOptions();

                        return false;
                    }else{
                        showFavFragment();

                        return true;
                    }
                }else if(itemId == R.id.menu_profile){


                    if(firebaseAuth.getCurrentUser() == null){
                        Toast.makeText(MainActivity.this, "Login Required...", Toast.LENGTH_SHORT).show();
                        startLoginOptions();

                        return false;
                    }else{
                        showProfileFragment();

                        return true;
                    }
                }else{

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
        binding.toolbarTitleTv.setText("Profile");

        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "Profile Fragment");
        fragmentTransaction.commit();
    }

    private void showFavFragment() {
        binding.toolbarTitleTv.setText("Fav");

        FavFragment fragment = new FavFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "Fav Fragment");
        fragmentTransaction.commit();
    }

    private void showChatsFragment() {
        binding.toolbarTitleTv.setText("Chats");

        ChatsFragment fragment = new ChatsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "Chats Fragment");
        fragmentTransaction.commit();
    }

    private void showHomeFragment() {
        binding.toolbarTitleTv.setText("Home");

        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment, "Home Fragment");
        fragmentTransaction.commit();
    }
}