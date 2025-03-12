package com.example.stayconnect;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stayconnect.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;

    private final static String TAG = "CHANGE_PASS_TAG";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String currentPassword = "";
    private String newPassword = "";
    private String confirmNewPassword = "";

    private void validateData() {

        currentPassword = binding.currentPasswordEt.getText().toString();
        newPassword = binding.newPasswordEt.getText().toString();
        confirmNewPassword = binding.confirmPasswordEt.getText().toString();

        Log.d(TAG, "validateData: currentPassword: "+currentPassword);
        Log.d(TAG, "validateData: newPassword: "+newPassword);
        Log.d(TAG, "validateData: confirmNewPassword: "+confirmNewPassword);

        if (currentPassword.isEmpty()) {

            binding.currentPasswordEt.setError("Enter Current Password!");
            binding.currentPasswordEt.requestFocus();
        } else if (newPassword.isEmpty()) {

            binding.newPasswordEt.setError("Enter New Password!");
            binding.newPasswordEt.requestFocus();
        } else if (confirmNewPassword.isEmpty()) {

            binding.confirmPasswordEt.setError("Enter Confirm Password!");
            binding.confirmPasswordEt.requestFocus();
        } else if (!newPassword.equals(confirmNewPassword)) {

            binding.confirmPasswordEt.setError("Password doesn't match!");
            binding.confirmPasswordEt.requestFocus();
        }else {
            authenticateUserForUpdatePassword();
        }
    }

    private void authenticateUserForUpdatePassword() {
        Log.d(TAG, "authenticateUserForUpdatePassword: ");

        progressDialog.setMessage("Authenticating User");
        progressDialog.show();

        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);
        firebaseUser.reauthenticate(credential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        updatePassword();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Toast.makeText(ChangePasswordActivity.this, "Failed to authenticate due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePassword() {
        Log.d(TAG, "updatePassword: ");
        
        progressDialog.setMessage("Updating password...");
        progressDialog.show();
        
        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ChangePasswordActivity.this, "Password Updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);    
                        progressDialog.dismiss();
                        Toast.makeText(ChangePasswordActivity.this, "Failed to update password due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}