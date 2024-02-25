package com.chat_app.talkbox;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class TalkBox extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }
}
