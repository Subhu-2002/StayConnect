package com.example.stayconnect.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stayconnect.Utils;
import com.example.stayconnect.adapters.AdapterChat;
import com.example.stayconnect.databinding.ActivityChatBinding;
import com.example.stayconnect.models.ModelChat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;

    private static final String TAG = "CHAT_TAG";

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    private String ownerUid = "";

    private String myUid = "";

    private String chatPath = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);


        ownerUid = getIntent().getStringExtra("ownerUid");
        myUid = firebaseAuth.getUid();
        chatPath = Utils.chatPath(ownerUid, myUid);
        Log.d(TAG, "onCreate: ownerUid: " + ownerUid);
        Log.d(TAG, "onCreate: myUid: " + myUid);
        Log.d(TAG, "onCreate: chatPath: " + chatPath);


        loadMessages();


        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.attachFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void validateData() {
        Log.d(TAG, "validateData: ");

        String message = binding.messageEt.getText().toString().trim();
        long timestamp = System.currentTimeMillis();


        if (message.isEmpty()) {
            Toast.makeText(this, "Enter message to send", Toast.LENGTH_SHORT).show();
        } else {
            sendMessage("TEXT", message, timestamp);
        }
    }

    private void sendMessage(String messageType, String message, long timestamp) {
        Log.d(TAG, "sendMessage: messageType: " + messageType);
        Log.d(TAG, "sendMessage: message: " + message);
        Log.d(TAG, "sendMessage: timestamp: " + timestamp);

        loadOwnerDetails();
        loadMessages();

        progressDialog.setMessage("Sending message...");
        progressDialog.show();

        DatabaseReference refChat = FirebaseDatabase.getInstance().getReference("Chats");

        String keyId = "" + refChat.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("messageId", "" + keyId);
        hashMap.put("messageType", "" + messageType);
        hashMap.put("message", "" + message);
        hashMap.put("fromUid", "" + myUid);
        hashMap.put("toUid", "" + ownerUid);
        hashMap.put("timestamp", timestamp);

        refChat.child(chatPath).child(keyId).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                binding.messageEt.setText("");
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: ", e);
                progressDialog.dismiss();
                Toast.makeText(ChatActivity.this, "Failed to send message due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOwnerDetails() {

        Log.d(TAG, "loadOwnerDetails: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(ownerUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try {
                    String name = "" + snapshot.child("name").getValue();
                    Log.d(TAG, "onDataChange: name: " + name);

                    binding.toolbarTitleTv.setText(name);
                } catch (Exception e) {
                    Log.e(TAG, "onDataChange: ", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMessages() {

        Log.d(TAG, "loadMessages: ");

        ArrayList<ModelChat> chatArrayList = new ArrayList<>();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.child(chatPath).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                chatArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    try {
                        ModelChat modelChat = ds.getValue(ModelChat.class);

                        chatArrayList.add(modelChat);
                    } catch (Exception e) {
                        Log.e(TAG, "loadMessages: onDataChange: ", e);
                    }
                }

                AdapterChat chatAdapter = new AdapterChat(ChatActivity.this, chatArrayList);
                binding.chatRv.setAdapter(chatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: ");
            }
        });
    }
}