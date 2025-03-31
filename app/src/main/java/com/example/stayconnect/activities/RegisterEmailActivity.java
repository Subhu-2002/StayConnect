package com.example.stayconnect.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stayconnect.Utils;
import com.example.stayconnect.databinding.ActivityRegisterEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterEmailActivity extends AppCompatActivity {

    private ActivityRegisterEmailBinding binding;

    private static final String TAG = "REGISTER_TAG";

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding = ActivityRegisterEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validateData();
            }
        });
    }


    private String email, password, cpassword;

    private void validateData() {

        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString();
        cpassword = binding.confirmPasswordEt.getText().toString();

        Log.d(TAG, "validateData: email: " + email);
        Log.d(TAG, "validateData: password: " + password);
        Log.d(TAG, "validateData: cpassword: " + cpassword);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            binding.emailEt.setError("Invalid Email Pattern");
            binding.emailEt.requestFocus();
        } else if (password.isEmpty()) {

            binding.passwordEt.setError("Enter Password");
            binding.passwordEt.requestFocus();
        } else if (!password.equals(cpassword)) {

            binding.confirmPasswordEt.setError("Password doesn't match");
            binding.confirmPasswordEt.requestFocus();
        } else {

            registerUser();
        }
    }

    private void registerUser() {

        progressDialog.setMessage("Creating Account");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: Register Successfully");

                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        Toast.makeText(RegisterEmailActivity.this, "Failed Due To " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    private void updateUserInfo() {

        progressDialog.setMessage("Saving User Info");

        long timestamp = System.currentTimeMillis();
        Log.d(TAG, "updateUserInfo: timestamp: "+timestamp);
        String formatteddate = Utils.formatTimestampDate(timestamp);
        Log.d(TAG, "updateUserInfo: formattedDate: "+formatteddate);
        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        String registerUserUid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "");
        hashMap.put("phoneCode", "");
        hashMap.put("phoneNumber", "");
        hashMap.put("dob", "");
        hashMap.put("userType", "Email");
        hashMap.put("typingTo", "");
        hashMap.put("timeStamp", timestamp);
        hashMap.put("onlineStatu", true);
        hashMap.put("email", registerUserEmail);
        hashMap.put("uid", registerUserUid);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Info saved...");
                        progressDialog.dismiss();

                        startActivity(new Intent(RegisterEmailActivity.this, ProfileEditActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Toast.makeText(RegisterEmailActivity.this, "Failed to save info due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        
                    }
                });
    }
}