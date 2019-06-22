package com.ds.zibzy.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.zibzy.ChatActivity;
import com.ds.zibzy.R;
import com.ds.zibzy.models.ModelUsers;
import com.squareup.picasso.Picasso;

import java.util.List;

import es.dmoral.toasty.Toasty;


public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    List<ModelUsers> usersList;

    public AdapterUsers(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_users,viewGroup,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        final String hisUID = usersList.get(i).getUid();
        String userImage = usersList.get(i).getImage();
        String userName = usersList.get(i).getName();
        final String userEmail = usersList.get(i).getEmail();

        myHolder.frndname.setText(userName);
        myHolder.frndemail.setText(userEmail);
        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.orange_face)
                    .into(myHolder.frndavatar);
        }
        catch (Exception e){

        }

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUID);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView frndavatar;
        TextView frndname, frndemail;

        public MyHolder(@NonNull View itemView){
            super(itemView);

            //init views
            frndavatar=itemView.findViewById(R.id.frndavatar);
            frndname=itemView.findViewById(R.id.frndname);
            frndemail = itemView.findViewById(R.id.frndemail);

        }
    }

}
