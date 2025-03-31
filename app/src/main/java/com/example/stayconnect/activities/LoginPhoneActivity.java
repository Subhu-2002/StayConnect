package com.example.stayconnect.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stayconnect.databinding.ActivityLoginPhoneBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {

    private ActivityLoginPhoneBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;

    private String mVerificationId;

    private static final String TAG = "LOGIN_PHONE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        phoneLoginCallBack();


        binding = ActivityLoginPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.sendOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        binding.resendOtpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resendVerificationCode();

            }
        });

        binding.verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String otp = binding.otpTil.toString().trim();

                if(otp.isEmpty()){

                    binding.otpTil.setError("Enter OTP");
                    binding.otpTil.requestFocus();
                } else if (otp.length() < 6) {

                    binding.otpTil.setError("OTP length must be 6 characters");
                    binding.otpTil.requestFocus();
                }else{
                    verifyPhoneNumberWithCode(mVerificationId, otp);
                }
            }
        });
    }

    private String phoneCode = "", phoneNumber = "", phoneNumberWithCode = "";

    private void validateData(){

        phoneCode = binding.phoneCodeTil.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();
        phoneNumberWithCode = phoneCode+phoneNumber;

        Log.d(TAG, "validateData: phoneCode: "+phoneCode);
        Log.d(TAG, "validateData: phoneNumber: "+phoneNumber);
        Log.d(TAG, "validateData: phoneNumberWithCode: "+phoneNumberWithCode);

        if(phoneNumber.isEmpty()){

            binding.phoneNumberEt.setError("Enter Phone Number");
            binding.phoneNumberEt.requestFocus();
        }else {
            startPhoneNumberVerification();
        }
    }

    private void startPhoneNumberVerification(){

        Log.d(TAG, "startPhoneNumberVerification: ");

        progressDialog.setMessage("Sending OTP to "+phoneNumberWithCode);
        progressDialog.show();


        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder()
                        .setPhoneNumber(phoneNumberWithCode)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void phoneLoginCallBack(){

        Log.d(TAG, "phoneLoginCallBack: ");

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted: ");

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e(TAG, "onVerificationFailed: ", e);

                progressDialog.dismiss();

                Toast.makeText(LoginPhoneActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(s, token);

                mVerificationId = s;
                forceResendingToken = token;

                progressDialog.dismiss();

                binding.phoneInputRl.setVisibility(View.INVISIBLE);
                binding.otpInputRl.setVisibility(View.VISIBLE);

                Toast.makeText(LoginPhoneActivity.this, "OTP sent to "+phoneNumberWithCode, Toast.LENGTH_SHORT).show();

                binding.loginLabelTv.setText("Please type the verification code sent to "+phoneNumberWithCode);
            }
        };
    }

    private void verifyPhoneNumberWithCode(String verificationId, String otp){

        Log.d(TAG, "verifyPhoneNumberWithCode: verificationId: "+verificationId);
        Log.d(TAG, "verifyPhoneNumberWithCode: OTP: "+otp);

        progressDialog.setMessage("Verifying OTP");
        progressDialog.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        signInWithPhoneAuthCredential(credential);

    }

    private void resendVerificationCode(){

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){

        Log.d(TAG, "signInWithPhoneAuthCredential: ");
        progressDialog.setMessage("Logging In");
        progressDialog.show();

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        Log.d(TAG, "onSuccess: ");

                        if (authResult.getAdditionalUserInfo().isNewUser()) {

                            Log.d(TAG, "onSuccess: New User, Account Created...");

                            updateUserInfoDb();
                        }else{

                            Log.d(TAG, "onSuccess: Existing User, Logged In");

                            startActivity(new Intent(LoginPhoneActivity.this, ProfileEditActivity.class));
                            finishAffinity();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);

                        progressDialog.dismiss();

                        Toast.makeText(LoginPhoneActivity.this, "Failed To Login Due To "+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void updateUserInfoDb(){
        Log.d(TAG, "updateUserInfoDb: ");

        progressDialog.setMessage("Saving User Info...");
        progressDialog.show();

        long timeStamp = System.currentTimeMillis();
        String registerUserUid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "");
        hashMap.put("phoneCode", ""+phoneCode);
        hashMap.put("phoneNumber", ""+phoneNumber);
        hashMap.put("dob", "");
        hashMap.put("userType", "Phone");
        hashMap.put("typingTo", "");
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("onlineStatus", true);
        hashMap.put("email", "");
        hashMap.put("uid", registerUserUid);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Info saved...");
                        progressDialog.dismiss();

                        startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Toast.makeText(LoginPhoneActivity.this, "Failed to save info due to " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }
}