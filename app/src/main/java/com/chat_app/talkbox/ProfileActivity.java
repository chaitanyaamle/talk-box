package com.chat_app.talkbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chat_app.talkbox.model.UserDisplayPic;
import com.chat_app.talkbox.model.UserInfo;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    public String username = "";
    public String contactNo = "";
    public Uri profilePicUri = null;
    CircleImageView photo;
    String currUid = "";
    ProgressBar prgBar;
    Button completeAction;
    EditText usernameEt;
    ImageView editProfilePic, deleteProfilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().hide();

        FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
        currUid = currUser.getUid();

        usernameEt = findViewById(R.id.et_username);
        photo = findViewById(R.id.profile_image);
        ImageView back = findViewById(R.id.back_btn);
        completeAction = findViewById(R.id.complete_action);
        EditText contactNoEt = findViewById(R.id.et_contact_no);
        editProfilePic = findViewById(R.id.edit_profile_pic);
        deleteProfilePic = findViewById(R.id.delete_profile_pic);

        prgBar = findViewById(R.id.prgBar);

        String userPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        contactNo = userPhoneNumber;
        contactNoEt.setText(userPhoneNumber);

        fetchUserDetails();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProfileActivity.this, Phone_SignIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        editProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(ProfileActivity.this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start(1010);
            }
        });

        deleteProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference userDisplayRef = FirebaseDatabase.getInstance().getReference("UserDisplayPic").child(contactNo);
                userDisplayRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserDisplayPic userDisplayPic = snapshot.getValue(UserDisplayPic.class);
                            StorageReference displayPicRef = FirebaseStorage.getInstance().getReferenceFromUrl(userDisplayPic.getUserDisplayPic());
                            displayPicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    profilePicUri = null;
                                    userDisplayRef.removeValue();
                                    editProfilePic.setVisibility(View.VISIBLE);
                                    deleteProfilePic.setVisibility(View.INVISIBLE);
                                    photo.setImageResource(R.drawable.user_basic_img);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        completeAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserInfo").child(currUid);
                if(usernameEt.getText().toString().trim().isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please enter your username!",Toast.LENGTH_SHORT).show();
                }
                else if(contactNoEt.getText().toString().trim().isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please enter your contact no!",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(profilePicUri != null){
                        prgBar.setVisibility(View.VISIBLE);
                        completeAction.setVisibility(View.INVISIBLE);
                        uploadImageToFirebaseStorage();
                    }
                    else {
                        prgBar.setVisibility(View.VISIBLE);
                        completeAction.setVisibility(View.INVISIBLE);
                        username = usernameEt.getText().toString();
                        UserInfo userInfo = new UserInfo(currUid,username,contactNo);
                        userRef.setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                prgBar.setVisibility(View.INVISIBLE);
                                completeAction.setVisibility(View.VISIBLE);
                                if(task.isSuccessful()){
                                    startActivity(new Intent(ProfileActivity.this,ChatHomeActivity.class));
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("DATABASE ERROR",e.toString());
                            }
                        });
                    }
                }
            }
        });
    }

    private void fetchUserDetails() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserInfo").child(currUid);
        DatabaseReference userDisplayRef = FirebaseDatabase.getInstance().getReference("UserDisplayPic").child(contactNo);
        userRef.keepSynced(true);
        userDisplayRef.keepSynced(true);
        userDisplayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    editProfilePic.setVisibility(View.INVISIBLE);
                    deleteProfilePic.setVisibility(View.VISIBLE);
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

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    UserInfo userInfo = snapshot.getValue(UserInfo.class);
                    usernameEt.setText(userInfo.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadImageToFirebaseStorage() {
        String filename = UUID.randomUUID().toString();
        StorageReference ref = FirebaseStorage.getInstance().getReference("Profile_pic").child(filename);
        if(profilePicUri != null){
            StorageTask uploadTask = ref.putFile(profilePicUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<? extends Object>>() {
                @Override
                public Task<? extends Object> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = (Uri) task.getResult();
                        SaveUserInfoToFirebase(downloadUri.toString());
                    }
                }
            });
        }
    }

    private void SaveUserInfoToFirebase(String downloadUri) {
        username = usernameEt.getText().toString();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserInfo").child(currUid);
        DatabaseReference userDisplayRef = FirebaseDatabase.getInstance().getReference("UserDisplayPic").child(contactNo);
        UserInfo userInfo = new UserInfo(currUid,username,contactNo);
        UserDisplayPic userDisplayPic = new UserDisplayPic(currUid,downloadUri);
        userRef.setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                userDisplayRef.setValue(userDisplayPic);
                prgBar.setVisibility(View.INVISIBLE);
                completeAction.setVisibility(View.VISIBLE);
                if(task.isSuccessful()){
                    startActivity(new Intent(ProfileActivity.this,ChatHomeActivity.class));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DATABASE ERROR",e.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1010){
            profilePicUri = data.getData();
            photo.setImageURI(data.getData());
        }
    }
}