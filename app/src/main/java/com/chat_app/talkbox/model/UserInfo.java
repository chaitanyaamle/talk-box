package com.chat_app.talkbox.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;


public class UserInfo implements Parcelable {
    public String uid;
    public String username;
    public String contactNo;
    public String profilePic;

    UserInfo(){ }

    public UserInfo(String uid, String username, String contactNo){
        this.uid = uid;
        this.username = username;
        this.contactNo = contactNo;
    }

    protected UserInfo(Parcel in) {
        uid = in.readString();
        username = in.readString();
        contactNo = in.readString();
        profilePic = in.readString();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }

    public String getProfilePic() {
        return profilePic;
    }

    public String getUsername() {
        return username;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setUsername(String username) { this.username = username; }

    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(username);
        parcel.writeString(contactNo);
        parcel.writeString(profilePic);
    }
}
