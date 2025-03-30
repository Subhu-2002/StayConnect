package com.example.stayconnect.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.example.stayconnect.FilterChats;
import com.example.stayconnect.R;
import com.example.stayconnect.Utils;
import com.example.stayconnect.activities.ChatActivity;
import com.example.stayconnect.databinding.RowChatsBinding;
import com.example.stayconnect.models.ModelAd;
import com.example.stayconnect.models.ModelChats;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterChats extends RecyclerView.Adapter<AdapterChats.HolderChats> implements Filterable {

    private RowChatsBinding binding;

    private static final String TAG = "ADAPTER_CHATS_TAG";

    private Context context;

    public ArrayList<ModelChats> chatsArrayList;
    private ArrayList<ModelChats> filterList;

    private FilterChats filter;

    private String myUid;

    private FirebaseAuth firebaseAuth;


    public AdapterChats(Context context, ArrayList<ModelChats> chatsArrayList) {
        this.context = context;
        this.chatsArrayList = chatsArrayList;
        this.filterList = chatsArrayList;


        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public HolderChats onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowChatsBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderChats(binding.getRoot());

    }

    @Override
    public void onBindViewHolder(@NonNull HolderChats holder, int position) {

        ModelChats modelChats = chatsArrayList.get(position);

        loadLastMessage(modelChats, holder);





        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ownerUid = modelChats.getOwnerUid();

                if (ownerUid != null) {

                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("ownerUid", ownerUid);
                    context.startActivity(intent);
                }
            }
        });

    }

    private void loadLastMessage(ModelChats modelChats, HolderChats holder) {

        String chatKey = modelChats.getChatKey();

        Log.d(TAG, "loadLastMessage: chatKey: " + chatKey);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.child(chatKey).limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String fromUid = "" + ds.child("fromUid").getValue();
                            String toUid = "" + ds.child("toUid").getValue();
                            String message = "" + ds.child("message").getValue();
                            String messageType = "" + ds.child("messageType").getValue();
                            String messageId = "" + ds.child("messageId").getValue();
                            long timestamp = (Long) ds.child("timestamp").getValue();

                            String formattedDateTime = Utils.formatTimestampDateTime(timestamp);


                            modelChats.setMessage(message);
                            modelChats.setMessageId(messageId);
                            modelChats.setMessageType(messageType);
                            modelChats.setFromUid(fromUid);
                            modelChats.setToUid(toUid);
                            modelChats.setTimestamp(timestamp);

                            holder.dateTimeTv.setText(formattedDateTime);

                            if (messageType.equals("TEXT")) {
                                holder.lastMessageTv.setText(message);
                            }


                            loadOwnerUserInfo(modelChats, holder);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadOwnerUserInfo(ModelChats modelChats, HolderChats holder) {

        String fromUid = modelChats.getFromUid();
        String toUid = modelChats.getToUid();

        String ownerUid;
        if (fromUid.equals(myUid)) {
            ownerUid = toUid;
        } else {
            ownerUid = fromUid;
        }

        modelChats.setOwnerUid(ownerUid);


        Log.d(TAG, "loadOwnerUserInfo: fromUid: " + fromUid);
        Log.d(TAG, "loadOwnerUserInfo: toUid: " + toUid);
        Log.d(TAG, "loadOwnerUserInfo: ownerUid: " + ownerUid);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(ownerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String name = "" + snapshot.child("name").getValue();
                        String publicId = "" + snapshot.child("profilePicture").getValue();

                        String imageUrl = MediaManager.get().url().generate(publicId);
//                        String profileImageUrl = ""+ snapshot.child("profileImageUrl").getValue();


                        modelChats.setName(name);
//                        modelChats.setProfileImageUrl(profileImageUrl);

                        holder.nameTv.setText(name);

                        try{
                            Glide.with(context)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile_white)
                                    .into(binding.profileIv);
                        }catch (Exception e){
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return chatsArrayList.size();
    }

    @Override
    public Filter getFilter() {

        if (filter == null) {
            filter = new FilterChats(this, filterList);
        }
        return filter;
    }

    class HolderChats extends RecyclerView.ViewHolder {

        ShapeableImageView profileIv;
        TextView nameTv, lastMessageTv, dateTimeTv;

        public HolderChats(@NonNull View itemView) {
            super(itemView);

            profileIv = binding.profileIv;
            nameTv = binding.nameTv;
            lastMessageTv = binding.lastMessageTv;
            dateTimeTv = binding.dateTimeTv;

        }
    }
}