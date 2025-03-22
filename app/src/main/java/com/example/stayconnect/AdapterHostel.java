package com.example.stayconnect;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stayconnect.databinding.RowServicesBinding;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterHostel extends RecyclerView.Adapter<AdapterHostel.HolderHostel> implements Filterable {

    private RowServicesBinding binding;

    private static final String TAG = "ADAPTER_SERVICE_TAG";

    private Context context;

    public ArrayList<ModelHostel> hostelArrayList;
    private ArrayList<ModelHostel> filterList;

    private FilterService filter;

    private FirebaseAuth firebaseAuth;



    public AdapterHostel(Context context, ArrayList<ModelHostel> hostelArrayList) {
        this.context = context;

        if(hostelArrayList == null){
            this.hostelArrayList = new ArrayList<>();
        }else{
            this.hostelArrayList = hostelArrayList;
        }
        this.filterList = hostelArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderHostel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowServicesBinding.inflate(LayoutInflater.from(context), parent,false);
        return new HolderHostel(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderHostel holder, int position) {

        ModelHostel modelHostel = hostelArrayList.get(position);

        String hostelName = modelHostel.getHostelName();
        String hostelAddress = modelHostel.getHostelAddress();
        String descriptionEt = modelHostel.getDescriptionEt();
        String rent = modelHostel.getRent();
        String hostelId = modelHostel.getId();

//        loadHostelFirstImage(modelHostel, holder);

        if(firebaseAuth.getCurrentUser() != null){
            checkIsFavorite(modelHostel, holder);
        }


        holder.titleTv.setText(hostelName);
        holder.descriptionTv.setText(descriptionEt);
        holder.priceTv.setText(rent);
        holder.addressTv.setText(hostelAddress);




        holder.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean favorite = modelHostel.isFavorite();
                if(favorite){

                    Utils.removeFromFavorite(context, hostelId);
                }else{

                    Utils.addToFavorite(context, hostelId);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return hostelArrayList.size();
    }

    private void checkIsFavorite(ModelHostel modelHostel, HolderHostel holder) {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(modelHostel.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        boolean favorite = snapshot.exists();

                        modelHostel.setFavorite(favorite);

                        if(favorite){
                            holder.favBtn.setImageResource(R.drawable.ic_fav_yes);
                        }else {

                            holder.favBtn.setImageResource(R.drawable.ic_fav_no);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadHostelFirstImage(ModelHostel modelHostel, HolderHostel holder) {
        Log.d(TAG, "loadHostelFirstImage: ");

        String hostelId = modelHostel.getId();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(hostelId).child("Images").limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){

                            String imageUrl = ""+ds.child("imageUrl");
                            Log.d(TAG, "onDataChange: imageUrl :"+imageUrl);

                            try {
                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_image_gray)
                                        .into(holder.imageIv);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: Cancelled");;
                    }
                });
    }


    @Override
    public Filter getFilter() {

        if (filter == null){
            filter = new FilterService(this, filterList);
        }

        return filter;
    }

    class HolderHostel extends RecyclerView.ViewHolder{

        ShapeableImageView imageIv;
        TextView titleTv, descriptionTv, addressTv, priceTv;
        ImageButton favBtn;

        public HolderHostel(@NonNull View itemView) {
            super(itemView);

            imageIv = binding.imageIv;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            favBtn = binding.favBtn;
            addressTv = binding.addressTv;
            priceTv = binding.priceTv;

        }
    }
}
