package com.example.flowerboutique;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class BoutiqueApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
