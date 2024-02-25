package com.chat_app.talkbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chat_app.talkbox.model.UserDisplayPic;
import com.chat_app.talkbox.model.UserInfo;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.xwray.groupie.OnItemClickListener;


import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    GroupieAdapter adapter = new GroupieAdapter();

    String[] permission = {"android.permission.READ_CONTACTS"};
    String currUserPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        getSupportActionBar().hide();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permission,80);
        }

        recyclerView = findViewById(R.id.contacts_recycler);
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_bar);

        bottomNavigationView.setSelectedItemId(R.id.people);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.people: break;
                    case R.id.home:
                        overridePendingTransition(0,0);
                        startActivity(new Intent(ContactsActivity.this,ChatHomeActivity.class));
                        break;
                }
                return false;
            }
        });

        CircleImageView photo = findViewById(R.id.profile);

        currUserPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
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
                Intent intent = new Intent(ContactsActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ContactsActivity.this, Phone_SignIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null,null);
        while(Objects.requireNonNull(cursor).moveToNext()){
            int cur_name = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int cur_number = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String name = cursor.getString(cur_name);
            String number = cursor.getString(cur_number);
            // Finds the contact in our database through the Firebase Query to know whether that contact is using our app or not.
            findUsers(number);
        }
    }

    private void findUsers(final String number){
        //Log.d("Numbers",number);
        String contactNo = number;
        if(!contactNo.contains("+91")){
            contactNo = "+91"+number;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserInfo");
        Query query = userRef.orderByChild("contactNo").equalTo(contactNo);
        query.keepSynced(true);
        String finalContactNo = contactNo;
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    final UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                    if(userInfo != null){
                        String currUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        if(!userInfo.getUid().equals(currUid)){
                            Log.d("From Message Model",userInfo.getUid());
                            Log.d("Curr UID",currUid);
                            adapter.add(new UserContactItem(userInfo));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 80){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                initData();
            }
            else{
                Toast.makeText(getApplicationContext(),"PERMISSION DENIED!",Toast.LENGTH_SHORT).show();
            }
        }
    }
}

class UserContactItem extends Item<GroupieViewHolder>{
    UserInfo userInfo;
    public UserContactItem(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
        Context context = viewHolder.itemView.getContext();
        TextView username = viewHolder.itemView.findViewById(R.id.ci_username);
        TextView phoneNo = viewHolder.itemView.findViewById(R.id.ci_contact);
        ImageView photo = viewHolder.itemView.findViewById(R.id.ci_profile_image);

        username.setText(userInfo.getUsername());
        phoneNo.setText(userInfo.getContactNo());

        viewHolder.itemView.findViewById(R.id.ll1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ChatActivity.class);
                intent.putExtra("UserInfo", userInfo);
                context.startActivity(intent);
            }
        });

        DatabaseReference userDisplayRef = FirebaseDatabase.getInstance().getReference("UserDisplayPic").child(userInfo.getContactNo());
        userDisplayRef.keepSynced(true);
        userDisplayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserDisplayPic userDisplayPic = snapshot.getValue(UserDisplayPic.class);
                    if(userDisplayPic != null){
                        Picasso.get().load(userDisplayPic.getUserDisplayPic()).into(photo);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getLayout() {
        return R.layout.contacts_item;
    }
}