package com.example.meetup.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.meetup.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class password_reset extends AppCompatActivity {

    EditText mEmail;
    Button mSubmitBtn, mBackBtn;
    FirebaseAuth firebaseAuth;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        mEmail = findViewById(R.id.Email);
        mSubmitBtn = findViewById(R.id.submitbtn);
        mBackBtn = findViewById(R.id.backtologinbtn);
        mProgressBar = findViewById(R.id.progressBar);
        firebaseAuth = FirebaseAuth.getInstance();

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                loading(true);

                firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        loading(false);
                        Toast.makeText(password_reset.this,"Password reset link Send Successfully",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loading(false);
                        Toast.makeText(password_reset.this,"Error! Check Entered Email" ,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

    }
    private void loading(boolean isloading){
        if (isloading){
            mSubmitBtn.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }else{
            mProgressBar.setVisibility(View.INVISIBLE);
            mSubmitBtn.setVisibility(View.VISIBLE);
        }
    }
}