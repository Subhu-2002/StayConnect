package com.example.stayconnect.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stayconnect.databinding.ActivityDeleteAccountBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DeleteAccountActivity extends AppCompatActivity {

    private ActivityDeleteAccountBinding binding;

    private static final String TAG = "DELETE_ACCOUNT_TAG";

    private FirebaseAuth firebaseAuth;

    private FirebaseUser firebaseUser;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });
    }

    private void deleteAccount() {

        Log.d(TAG, "deleteAccount: ");

        String myUid = firebaseAuth.getUid();

        progressDialog.setMessage("Deleting User Account");
        progressDialog.show();

        firebaseUser.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d(TAG, "onSuccess: Deleted");
                        progressDialog.setMessage("Deleting User Ads");

                        // Remove user Ads
                        DatabaseReference refUserAds = FirebaseDatabase.getInstance().getReference("Ads");
                        refUserAds.orderByChild("uid").equalTo(myUid)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for(DataSnapshot ds : snapshot.getChildren()){
                                                    ds.getRef().removeValue();
                                                }

                                                progressDialog.setMessage("Deleting User Data...");
                                                progressDialog.show();

                                                DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");
                                                refUsers.child(myUid)
                                                        .removeValue()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {

                                                                Log.d(TAG, "onSuccess: User data deleted...");
                                                                startMainActivity();

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {

                                                                Log.e(TAG, "onFailure: ", e);
                                                                progressDialog.dismiss();
                                                                Toast.makeText(DeleteAccountActivity.this, "Failed to delete user data due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                startMainActivity();
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Toast.makeText(DeleteAccountActivity.this, "Failed to delete account due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
    }

    private void startMainActivity(){
        Log.d(TAG, "startMainActivity: ");

        startActivity(new Intent(this, MainActivity.class));
    }
}