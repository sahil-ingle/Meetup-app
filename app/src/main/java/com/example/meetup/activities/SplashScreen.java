package com.example.meetup.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.meetup.R;
import com.google.firebase.auth.FirebaseAuth;


public class SplashScreen extends AppCompatActivity {
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        firebaseAuth = firebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (firebaseAuth.getCurrentUser() != null){
                    startActivity(new Intent(getApplicationContext(), profile.class));
                    finish();
                }else{
                    startActivity(new Intent(getApplicationContext(),Home.class));
                    finish();
                }
            }
        },3000);
    }
}