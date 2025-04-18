package com.example.stayconnect.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.example.stayconnect.Utils;
import com.example.stayconnect.activities.MainActivity;
import com.example.stayconnect.R;
import com.example.stayconnect.activities.ChangePasswordActivity;
import com.example.stayconnect.activities.DeleteAccountActivity;
import com.example.stayconnect.activities.ProfileEditActivity;
import com.example.stayconnect.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private static final String TAG = "PROFILE_TAG";

    private Context mContext;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;


    @Override
    public void onAttach(@NonNull Context context) {

        mContext = context;
        super.onAttach(context);
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentProfileBinding.inflate(LayoutInflater.from(mContext), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();

        binding.logoutCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseAuth.signOut();

                startActivity(new Intent(mContext, MainActivity.class));
                getActivity().finishAffinity();
            }
        });

        binding.editProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ProfileEditActivity.class));
            }
        });

        binding.changePasswordCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ChangePasswordActivity.class));
            }
        });

        binding.verifyAccountCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAccount();
            }
        });

        binding.deleteAccountCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        binding.deleteAccountCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, DeleteAccountActivity.class));
            }
        });
    }

    private void loadMyInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String dob = "" + snapshot.child("dob").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String profileImageUri = "" + snapshot.child("profileImageUri").getValue();
                        String timestamp = "" + snapshot.child("timeStamp").getValue();
                        Log.d(TAG, "onDataChange: timeStamp: " + timestamp);
                        String userType = "" + snapshot.child("userType").getValue();
                        String name = "" + snapshot.child("name").getValue();

                        String phone = phoneCode + phoneNumber;

                        if (timestamp.equals("null")) {
                            timestamp = "0";
                        }

                        String formattedDate = Utils.formatTimestampDate(Long.valueOf(timestamp));

                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.phoneTv.setText(phone);
                        binding.memberSinceTv.setText(formattedDate);
                        binding.dobTv.setText(dob);

                        if (userType.equals("Email")) {

                            boolean isVerified = firebaseAuth.getCurrentUser().isEmailVerified();
                            Log.d(TAG, "onDataChange: isVerified: "+isVerified);


                            if (isVerified) {

                                binding.verifyAccountCv.setVisibility(View.GONE);
                                binding.verificationStatusTv.setText("Verified");
                            } else {

                                binding.verifyAccountCv.setVisibility(View.VISIBLE);
                                binding.verificationStatusTv.setText("Not Verified");
                            }
                        } else {
                            binding.verifyAccountCv.setVisibility(View.GONE);
                            binding.verificationStatusTv.setText("Verified");
                        }


                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (snapshot.exists()) {

                                            String publicId = snapshot.child("profilePicture").getValue(String.class);

                                            if(publicId != null){
                                                String imageUrl = MediaManager.get().url().generate(publicId);

                                                try {
                                                    Glide.with(mContext)
                                                            .load(imageUrl)
                                                            .placeholder(R.drawable.ic_person_gray)
                                                            .into(binding.profileIv);

                                                } catch (Exception e) {
                                                    Log.e(TAG, "onDataChange: ", e);
                                                }
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void verifyAccount() {
        Log.d(TAG, "verifyAccount: ");

        progressDialog.setMessage("Sending account verification instructions to your email");
        progressDialog.show();

        firebaseAuth.getCurrentUser().sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d(TAG, "onSuccess: Sent");
                        progressDialog.dismiss();

                        Toast.makeText(mContext, "Account Verification instructions sent to your email", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Toast.makeText(mContext, "Failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}