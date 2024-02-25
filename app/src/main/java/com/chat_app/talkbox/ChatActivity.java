package com.chat_app.talkbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.chat_app.talkbox.model.MessageChatModel;
import com.chat_app.talkbox.model.UserDisplayPic;
import com.chat_app.talkbox.model.UserInfo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupieAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;

    MessageChatModel chatMessage = null;

    GroupieAdapter adapter = new GroupieAdapter();

    UserInfo userInfo;
    String chatContactNo;
    String recieverId;

    EditText messageET;
    ImageView sendBtn;

    String AES = "AES";
    String password = "7alkB0x";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();
        messageET = (EditText)findViewById(R.id.messageEt);
        sendBtn = (ImageView) findViewById(R.id.sendBtn);

        recyclerView = (RecyclerView)findViewById(R.id.chat_recycler);

        recyclerView.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(ChatActivity.this, RecyclerView.VERTICAL, false);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.scrollToPosition(adapter.getItemCount()-1);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int myMessageCount = adapter.getItemCount();
                int lastVisiblePosition =
                        manager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (myMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });

        listenforMessage();

        final ImagePopup imagePopup = new ImagePopup(this);
        imagePopup.setWindowHeight(920); // Optional
        imagePopup.setWindowWidth(920); // Optional
        imagePopup.setFullScreen(false);
        imagePopup.setHideCloseIcon(true);
        imagePopup.setImageOnClickClose(true);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                performSendMessage();
            }
        });

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, ChatHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        userInfo = (UserInfo) getIntent().getParcelableExtra("UserInfo");
        chatContactNo = userInfo.getContactNo();

        TextView chatUsername = findViewById(R.id.chat_username);
        chatUsername.setText(userInfo.getUsername());
        ImageView chatProfilePic = findViewById(R.id.chat_profile_image);

        DatabaseReference userDisplayRef = FirebaseDatabase.getInstance().getReference("UserDisplayPic").child(chatContactNo);
        userDisplayRef.keepSynced(true);
        userDisplayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserDisplayPic userDisplayPic = snapshot.getValue(UserDisplayPic.class);
                    if(userDisplayPic != null){
                        Picasso.get().load(userDisplayPic.getUserDisplayPic()).into(chatProfilePic);
                        imagePopup.initiatePopupWithPicasso(userDisplayPic.getUserDisplayPic());
                        findViewById(R.id.chat_profile_image).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                imagePopup.viewPopup();
                            }
                        });
                    }
                    else{
                        Picasso.setSingletonInstance(new Picasso.Builder(getApplicationContext()).build());
                        chatProfilePic.setImageResource(R.drawable.user_basic_img);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void listenforMessage() {
        try{
            String Sender = firebaseAuth.getCurrentUser().getUid();
            userInfo = (UserInfo) getIntent().getParcelableExtra("UserInfo");
            recieverId = userInfo.getUid();
            DatabaseReference mess_ref = FirebaseDatabase.getInstance().getReference().child("Messages").child(Sender).child(recieverId);
            mess_ref.keepSynced(true);
            mess_ref.addChildEventListener(new ChildEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    MessageChatModel chatMessage = snapshot.getValue(MessageChatModel.class);
                    String text = chatMessage.getText();
                    Calendar calendar = Calendar.getInstance();
                    String formatedtime = DateFormat.format("h:mm a", chatMessage.getTimestamp()).toString();
                    if (chatMessage.getFromId().equals(FirebaseAuth.getInstance().getUid())) {
                        try {
                            adapter.add(new ChatToItem(decrypt(text,password),formatedtime));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            adapter.add(new ChatFromItem(decrypt(text,password),formatedtime));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void performSendMessage() {
        try{
            String Sender = firebaseAuth.getCurrentUser().getUid();
            recieverId = userInfo.getUid();
            String receivername = userInfo.getUsername();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Messages").child(Sender).child(recieverId).push();
            DatabaseReference to_ref = FirebaseDatabase.getInstance().getReference("Messages").child(recieverId).child(Sender).push();
            String msg = messageET.getText().toString();
            String encodedMsg = encrypt(msg,password);
            if(Sender == null) return;
            chatMessage = new MessageChatModel(ref.getKey(),encodedMsg,Sender,recieverId, System.currentTimeMillis());
            if (TextUtils.isEmpty(msg)) {
                Toast.makeText(this, "Text Cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                ref.setValue(chatMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        messageET.setText("");
                    }
                });
                to_ref.setValue(chatMessage);
                DatabaseReference latest_message_ref = FirebaseDatabase.getInstance().getReference("Latest-Messages").child(Sender).child(recieverId);
                DatabaseReference latest_message_to_ref = FirebaseDatabase.getInstance().getReference("Latest-Messages").child(recieverId).child(Sender);
                latest_message_ref.setValue(chatMessage);
                latest_message_to_ref.setValue(chatMessage);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
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

class ChatFromItem extends Item<GroupieViewHolder> {
    private String text;
    private String time;

    public ChatFromItem(String text, String time) {
        this.text = text;
        this.time = time;
    }


    @Override
    public void bind(@NonNull GroupieViewHolder viewHolder, int position) {

        TextView message = viewHolder.itemView.findViewById(R.id.tv_left_item);
        TextView tvTime = viewHolder.itemView.findViewById(R.id.tv_from_time);
        tvTime.setText(time);
        message.setText(text);
    }

    @Override
    public int getLayout() {
        return R.layout.left_item;
    }
}

class ChatToItem extends Item<GroupieViewHolder>{
    private String text;
    private String time;

    public ChatToItem(String text, String time) {
        this.text = text;
        this.time = time;
    }

    @Override
    public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
        TextView message = viewHolder.itemView.findViewById(R.id.tv_right_item);
        TextView tvTime = viewHolder.itemView.findViewById(R.id.tv_to_time);
        tvTime.setText(time);
        message.setText(text);
    }

    @Override
    public int getLayout() {
        return R.layout.right_item;
    }
}