package com.example.meetup.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.meetup.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {

    MaterialButton mRegister, mLogin;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseAuth = firebaseAuth.getInstance();

        mLogin = findViewById(R.id.Loginbtn);
        mRegister = findViewById(R.id.Registerbtn);

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), profile.class));
            finish();
        }

        mLogin.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Login.class)));
        mRegister.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Register.class)));

    }
}