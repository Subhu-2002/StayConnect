package com.example.stayconnect.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.example.stayconnect.R;
import com.example.stayconnect.Utils;
import com.example.stayconnect.adapters.AdapterImageSlider;
import com.example.stayconnect.databinding.ActivityDetailsBinding;
import com.example.stayconnect.models.ModelAd;
import com.example.stayconnect.models.ModelImageSlider;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "AD_DETAILS_TAG";

    private ActivityDetailsBinding binding;

    private FirebaseAuth firebaseAuth;

    private String adId = "";

    private double adLatitude = 0;
    private double adLongitude = 0;

    private String ownerUid = "";
    private String ownerPhone = "";
    private String ownerName = "";

    private boolean favorite = false;

    private ArrayList<ModelImageSlider> imageSliderArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarEditBtn.setVisibility(View.GONE);
        binding.toobarDeleteBtn.setVisibility(View.GONE);
        binding.callBtn.setVisibility(View.GONE);
        binding.smsBtn.setVisibility(View.GONE);
        binding.chatBtn.setVisibility(View.GONE);


        adId = getIntent().getStringExtra("adId");
        Log.d(TAG, "onCreate: adId: " + adId);


        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorite();
        }

        loadAdDetails();
        loadImages();


        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.toobarDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(DetailsActivity.this);
                materialAlertDialogBuilder.setTitle("Delete Ad")
                        .setMessage("Are you sure you want to delete this Ad?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAd();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        binding.toolbarEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOptions();
            }
        });

        binding.toolbarFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favorite) {
                    Utils.removeFromFavorite(DetailsActivity.this, adId);
                } else {
                    Utils.addToFavorite(DetailsActivity.this, adId);
                }
            }
        });

        binding.ownerProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        binding.chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this, ChatActivity.class);
                intent.putExtra("ownerUid", ownerUid);
                intent.putExtra("adId", adId);
                Log.d(TAG, "onClick: ownerUid: " + ownerUid);
                Log.d(TAG, "onClick: adId: " + adId);
                startActivity(intent);
            }
        });

        binding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.callIntent(DetailsActivity.this, ownerPhone);
            }
        });

        binding.smsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.smsIntent(DetailsActivity.this, ownerPhone);
            }
        });

        binding.mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.mapIntent(DetailsActivity.this, adLatitude, adLongitude);
            }
        });
    }

    private void loadImages() {

        imageSliderArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId).child("Images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        imageSliderArrayList.clear();

                        for(DataSnapshot ds: snapshot.getChildren()){

                           String publicId = ds.getValue(String.class);

                            if (publicId != null) {

                                ModelImageSlider imageSlider = new ModelImageSlider();
                                imageSlider.setPublicId(publicId);
                                imageSliderArrayList.add(imageSlider);
                            }
                        }

                        AdapterImageSlider adapterImageSlider = new AdapterImageSlider(DetailsActivity.this, imageSliderArrayList);
                        binding.imageSliderVp.setAdapter(adapterImageSlider);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void editOptions() {

        Log.d(TAG, "editOptions: ");

        PopupMenu popupMenu = new PopupMenu(this, binding.toolbarEditBtn);

        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Edit");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == 0) {
                    Intent intent = new Intent(DetailsActivity.this, AddServiceActivity.class);
                    intent.putExtra("isEditMode", true);
                    intent.putExtra("adId", adId);
                    startActivity(intent);
                }

                return true;
            }
        });
    }


    private void loadAdDetails() {

        Log.d(TAG, "loadAdDetails: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {

                            ModelAd modelAd = snapshot.getValue(ModelAd.class);

                            ownerUid = modelAd.getUid();
                            ownerPhone = modelAd.getOwnerContactNumber();
                            ownerName = modelAd.getOwnerName();
                            String title = modelAd.getHostelName();
                            String address = modelAd.getHostelAddress();
                            String price = modelAd.getRent();
                            String description = modelAd.getDescription();
                            adLatitude = modelAd.getLatitude();
                            adLongitude = modelAd.getLongitude();

                            if (ownerUid.equals(firebaseAuth.getUid())) {
                                binding.toolbarEditBtn.setVisibility(View.VISIBLE);
                                binding.toobarDeleteBtn.setVisibility(View.VISIBLE);
                                binding.chatBtn.setVisibility(View.GONE);
                                binding.callBtn.setVisibility(View.GONE);
                                binding.smsBtn.setVisibility(View.GONE);
                            } else {
                                binding.toolbarEditBtn.setVisibility(View.GONE);
                                binding.toobarDeleteBtn.setVisibility(View.GONE);
                                binding.chatBtn.setVisibility(View.VISIBLE);
                                binding.callBtn.setVisibility(View.VISIBLE);
                                binding.smsBtn.setVisibility(View.VISIBLE);
                            }


                            binding.titleTv.setText(title);
                            binding.descriptionTv.setText(description);
                            binding.addressTv.setText(address);
                            binding.priceTv.setText(price);




                            loadOwnerDetails();


                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOwnerDetails() {

        Log.d(TAG, "loadOwnerDetails: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(ownerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String publicId = "" + snapshot.child("profilePicture").getValue();
                        String imageUrl = MediaManager.get().url().generate(publicId);
                        long timestamp = (Long) snapshot.child("timeStamp").getValue();


                        String formattedDate = Utils.formatTimestampDate(timestamp);

                        ownerPhone = phoneCode + "" + phoneNumber;


                        binding.ownerNameTv.setText(ownerName);
                        binding.memberSinceTv.setText("" + formattedDate);



                        try{
                            Glide.with(DetailsActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_person_gray)
                                    .into(binding.ownerProfileIv);
                        }catch (Exception e){
                            Log.e(TAG, "onDataChange: ", e);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        favorite = snapshot.exists();
                        Log.d(TAG, "onDataChange: favorite: " + favorite);
                        if (favorite) {
                            binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_yes);
                        } else {
                            binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_no);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteAd() {

        Log.d(TAG, "deletedAd: ");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        ref.child(adId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d(TAG, "onSuccess: Deleted");
                        Toast.makeText(DetailsActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        Toast.makeText(DetailsActivity.this, "Failed to delete due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}