package com.ds.zibzy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.zibzy.adapters.AdapterChat;
import com.ds.zibzy.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    ImageButton sendBtn;
    EditText messageEt;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    String hisUid;
    String myUid;
    String hisImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        recyclerView=findViewById(R.id.chat_RecyclerView);
        profileIv=findViewById(R.id.chatterIv);
        nameTv=findViewById(R.id.chatterTv);
        userStatusTv=findViewById(R.id.chatterOnlinestatus);
        sendBtn=findViewById(R.id.sendBtn);
        messageEt=findViewById(R.id.messageEt);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");

        Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUid);
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = ""+ ds.child("name").getValue();
                    hisImage = ""+ ds.child("image").getValue();

                    nameTv.setText(name);
                    try {
                        Picasso.get().load(hisImage).placeholder(R.drawable.whiteface).into(profileIv);
                    }
                    catch (Exception e) {
                        Picasso.get().load(R.drawable.whiteface).into(profileIv);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEt.getText().toString().trim();
                if(TextUtils.isEmpty(message)){
                    Toasty.warning(ChatActivity.this,"Type something to send a message", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendMessage(message);
                }
            }
        });

        readMessages();

        seenMessage();

    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReciever().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String,Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen",true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReciever().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReciever().equals(hisUid) && chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String,Object> hashMap =new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("reciever",hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);
        databaseReference.child("Chats").push().setValue(hashMap);

        messageEt.setText("");
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user!=null){
            //  mProfileTv.setText(user.getEmail());
            myUid = user.getUid();
        }
        else{
            Intent ma= new Intent(this,MainActivity.class);
            startActivity(ma);
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout)
        {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
