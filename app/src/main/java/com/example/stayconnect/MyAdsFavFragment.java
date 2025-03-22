package com.example.stayconnect;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.stayconnect.databinding.FragmentMyAdsFavBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MyAdsFavFragment extends Fragment {

    private static final String TAG = "FAV_TAG";

    private FragmentMyAdsFavBinding binding;

    private Context mContext;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelHostel> hostelArrayList;

    private AdapterHostel adapterHostel;



    public MyAdsFavFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {

        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsFavBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadAds();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try{
                    String query = s.toString();
                    adapterHostel.getFilter().filter(query);
                } catch (Exception e) {
                    Log.e(TAG, "onTextChanged: ", e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadAds(){

        Log.d(TAG, "loadAds: ");

        hostelArrayList = new ArrayList<>();

        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Users");
        favRef.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        hostelArrayList.clear();

                        for(DataSnapshot ds : snapshot.getChildren()){

                            String adId = ""+ ds.child("adId").getValue();
                            Log.d(TAG, "onDataChange: hostleId:"+adId);

                            DatabaseReference hostelRef = FirebaseDatabase.getInstance().getReference("Ads");
                            hostelRef.child(adId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                            try{

                                                ModelHostel modelHostel = snapshot.getValue(ModelHostel.class);
                                                hostelArrayList.add(modelHostel);

                                            } catch (Exception e) {
                                                Log.e(TAG, "onDataChange: ", e);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                adapterHostel = new AdapterHostel(mContext, hostelArrayList);
                                binding.adsRv.setAdapter(adapterHostel);
                            }
                        }, 500);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}