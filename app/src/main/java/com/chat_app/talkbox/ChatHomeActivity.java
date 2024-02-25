package com.chat_app.talkbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chat_app.talkbox.model.MessageChatModel;
import com.chat_app.talkbox.model.UserDisplayPic;
import com.chat_app.talkbox.model.UserInfo;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.github.marlonlom.utilities.timeago.TimeAgoMessages;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupieAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;

import java.security.MessageDigest;
import java.security.Timestamp;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatHomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    GroupieAdapter adapter = new GroupieAdapter();

    private FirebaseAuth firebaseAuth;

    HashMap<String, MessageChatModel> latestMessageMap = new HashMap<String, MessageChatModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_home);

        //getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = (RecyclerView)findViewById(R.id.recent_chats_recycler);

        recyclerView.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(ChatHomeActivity.this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        listenForLatestMessage();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_bar);

        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.people:
                        overridePendingTransition(0,0);
                        startActivity(new Intent(ChatHomeActivity.this,ContactsActivity.class));
                        break;
                    case R.id.home:
                        break;
                }
                return false;
            }
        });

        CircleImageView photo = findViewById(R.id.profile);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
                LatestMessageRow row = (LatestMessageRow) item;
                intent.putExtra("UserInfo",row.userInfo);
                startActivity(intent);
            }
        });

        String currUserPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        DatabaseReference userDisplayRef = FirebaseDatabase.getInstance().getReference("UserDisplayPic").child(currUserPhoneNumber);
        userDisplayRef.keepSynced(true);
        userDisplayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserDisplayPic userDisplayPic = snapshot.getValue(UserDisplayPic.class);
                    if(userDisplayPic != null){
                        Picasso.get().load(userDisplayPic.getUserDisplayPic()).into(photo);
                    }
                    else{
                        photo.setImageResource(R.drawable.user_basic_img);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatHomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ChatHomeActivity.this, Phone_SignIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void listenForLatestMessage() {
        String fromId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Latest-Messages").child(fromId);
        ref.keepSynced(true);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    MessageChatModel messageChatModel = snapshot.getValue(MessageChatModel.class);
                    latestMessageMap.put(snapshot.getKey(),messageChatModel);
                    refreshRecyclerViewMessage();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    MessageChatModel messageChatModel = snapshot.getValue(MessageChatModel.class);
                    latestMessageMap.put(snapshot.getKey(),messageChatModel);
                    refreshRecyclerViewMessage();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void refreshRecyclerViewMessage() {
        adapter.clear();
        for(Map.Entry<String, MessageChatModel> set: latestMessageMap.entrySet()){
            adapter.add(new LatestMessageRow(set.getValue()));
        }
    }
}

class LatestMessageRow extends Item<GroupieViewHolder> {
    MessageChatModel messageChatModel;
    String AES = "AES";
    String password = "7alkB0x";
    UserInfo userInfo = null;
    String currUid;

    public LatestMessageRow(MessageChatModel messageChatModel) {
        this.messageChatModel = messageChatModel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
        currUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TextView recentMessage = viewHolder.itemView.findViewById(R.id.rc_chat);
        TextView recentUsername = viewHolder.itemView.findViewById(R.id.rc_username);
        TextView recentTime = viewHolder.itemView.findViewById(R.id.rc_time_ago);
        ImageView recentProfileImage = viewHolder.itemView.findViewById(R.id.rc_profile_image);
        try {
            recentMessage.setText(decrypt(messageChatModel.getText(),password));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String chatPartnerId;
        if(messageChatModel.getFromId().equals(currUid)){
            chatPartnerId = messageChatModel.getToId();
        }else {
            chatPartnerId = messageChatModel.getFromId();
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("UserInfo").child(chatPartnerId);
        ref.keepSynced(true);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfo = snapshot.getValue(UserInfo.class);
                recentUsername.setText(userInfo.getUsername());
                String timeAgo = TimeAgo.using(messageChatModel.getTimestamp());
                String formatedtime = DateFormat.format("h:mm a", messageChatModel.getTimestamp()).toString();
                recentTime.setText(timeAgo);

                String currUserPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                DatabaseReference userDisplayRef = FirebaseDatabase.getInstance().getReference("UserDisplayPic").child(userInfo.getContactNo());
                userDisplayRef.keepSynced(true);
                userDisplayRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserDisplayPic userDisplayPic = snapshot.getValue(UserDisplayPic.class);
                            if(userDisplayPic != null){
                                Picasso.get().load(userDisplayPic.getUserDisplayPic()).into(recentProfileImage);
                            }
                            else{
                                recentProfileImage.setImageResource(R.drawable.user_basic_img);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getLayout() {
        return R.layout.recent_chats_item;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String encrypt(String Data, String password) throws Exception{
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.ENCRYPT_MODE,key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = Base64.getEncoder().encodeToString(encVal);
        return encryptedValue;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String decrypt(String outputString, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.DECRYPT_MODE,key);
        byte[] decodedValue = Base64.getDecoder().decode(outputString);
        byte[] decValue = c.doFinal(decodedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    private SecretKeySpec generateKey(String password) throws Exception{
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes,0,bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key,"AES");
        return secretKeySpec;
    }
}