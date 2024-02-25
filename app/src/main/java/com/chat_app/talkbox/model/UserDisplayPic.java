package com.chat_app.talkbox.model;

public class UserDisplayPic {
    public String uid;
    public String userDisplayPic;

    UserDisplayPic(){ }

    public UserDisplayPic(String uid, String userDisplayPic){
        this.uid = uid;
        this.userDisplayPic = userDisplayPic;
    }

    public String getUid() {
        return uid;
    }

    public String getUserDisplayPic() {
        return userDisplayPic;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUserDisplayPic(String userDisplayPic) {
        this.userDisplayPic = userDisplayPic;
    }
}
