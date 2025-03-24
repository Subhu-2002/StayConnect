package com.example.stayconnect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Utils {

    public static final String[] categories = {
            "Hostel",
            "Mess"
    };

    public static final int[] categoryIcons = {
            R.drawable.ic_category_hostel,
            R.drawable.ic_category_mess
    };

    public static void addToFavorite(Context context, String adId){

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){

            Toast.makeText(context, "You are not logged in!", Toast.LENGTH_SHORT).show();

        }else{

            long timeStamp = System.currentTimeMillis();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("adId", adId);
            hashMap.put("timestamp", timeStamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Added to Favorite...!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to add to favorite due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static void removeFromFavorite(Context context, String adId){

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){

            Toast.makeText(context, "You are not logged in!", Toast.LENGTH_SHORT).show();
        }else{
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(adId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Removed from favorite...!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to remove from favorite "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static String chatPath(String ownerUid, String yourUid){
        String[] arrayUids = new String[]{ownerUid, yourUid};

        Arrays.sort(arrayUids);

        String chatPath = arrayUids[0] +"_"+ arrayUids[1];

        return chatPath;
    }

    public static void callIntent(Context context, String phone){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"+Uri.encode(phone)));
        context.startActivity(intent);
    }

    public static void smsIntent(Context context, String phone){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+Uri.encode(phone)));
        context.startActivity(intent);
    }

    public static void mapIntent(Context context, double latitude, double longitude){

        Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + latitude +","+ longitude);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if(mapIntent.resolveActivity(context.getPackageManager()) != null){
            context.startActivity(mapIntent);
        }else{
            Toast.makeText(context, "Google MAP not installed!", Toast.LENGTH_SHORT).show();
        }
    }



    public static String formatTimestampDate(long timestamp){

        Date date = new Date(timestamp);

        String dateFormat = "dd/MM/yyyy";

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        String formattedDate = sdf.format(date);

        return formattedDate;
    }

    public static String formatTimestampDateTime(long timestamp){

        Date date = new Date(timestamp);

        String dateFormat = "dd/MM/yyyy hh:mm:a";

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        String formattedDate = sdf.format(date);

        return formattedDate;
    }

}
