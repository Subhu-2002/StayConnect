package com.example.stayconnect.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayconnect.R;
import com.example.stayconnect.Utils;
import com.example.stayconnect.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.HolderChat> {

    private Context context;

    private static final String TAG = "ADAPTER_CHAT_TAG";

    private ArrayList<ModelChat> chatArrayList;

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private FirebaseUser firebaseUser;


    public AdapterChat(Context context, ArrayList<ModelChat> chatArrayList) {
        this.context = context;
        this.chatArrayList = chatArrayList;

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public HolderChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);

            return new HolderChat(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent,false);

            return new HolderChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderChat holder, int position) {

        ModelChat modelChat = chatArrayList.get(position);

        String message = modelChat.getMessage();
        String messageType = modelChat.getMessageType();
        long timestamp = modelChat.getTimestamp();

        String formattedDate = Utils.formatTimestampDateTime(timestamp);

        if(messageType.equals("TEXT")){

            holder.messageTv.setVisibility(View.VISIBLE);
            holder.imageIv.setVisibility(View.GONE);

            holder.messageTv.setText(message);
        }else{

            holder.messageTv.setVisibility(View.GONE);
            holder.imageIv.setVisibility(View.VISIBLE);
        }

        holder.timeTv.setText(formattedDate);

    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {

        if(chatArrayList.get(position).getFromUid().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }

    class HolderChat extends RecyclerView.ViewHolder{

        TextView messageTv, timeTv;
        ImageView imageIv;

        public HolderChat(@NonNull View itemView) {
            super(itemView);

            imageIv = itemView.findViewById(R.id.imageIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
