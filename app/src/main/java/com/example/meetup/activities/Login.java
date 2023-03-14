package com.example.meetup.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meetup.R;
import com.example.meetup.utilities.Constants;
import com.example.meetup.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private PreferenceManager preferenceManager;

    EditText mEmail,mPasssword;
    Button mLoginBtn;
    TextView mRegisterBtn, mPasswordResetBtn;
    FirebaseAuth firebaseAuth;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mEmail = findViewById(R.id.Email);
        mPasssword = findViewById(R.id.Password);
        mLoginBtn = findViewById(R.id.Loginbtn);
        mRegisterBtn = findViewById(R.id.newhere);
        firebaseAuth = FirebaseAuth.getInstance();
        mPasswordResetBtn = findViewById(R.id.forgetpass);
        mProgressBar = findViewById(R.id.progressBar);
        preferenceManager = new PreferenceManager(getApplicationContext());

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loading(true);

                String email = mEmail.getText().toString().trim();
                String password = mPasssword.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    mEmail.setError("Enter Email");
                    loading(false);
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    mPasssword.setError("Enter Password");
                    loading(false);
                    return;
                }
                if (password.length() < 6){
                    mPasssword.setError("Password Must be more than 6 Characters");
                    loading(false);
                    return;
                }

                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task1) {
                        if (task1.isSuccessful()){
                            FirebaseFirestore database = FirebaseFirestore.getInstance();

                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .whereEqualTo(Constants.KEY_EMAIL, mEmail.getText().toString().trim())
                                    .whereEqualTo(Constants.KEY_PASSWORD, mPasssword.getText().toString().trim())
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                            preferenceManager.putString(Constants.KEY_INTEREST, documentSnapshot.getString(Constants.KEY_INTEREST));
                                            preferenceManager.putString(Constants.KEY_BIO, documentSnapshot.getString(Constants.KEY_BIO));
                                            preferenceManager.putString(Constants.KEY_IMG, documentSnapshot.getString(Constants.KEY_IMG));
                                            Toast.makeText(Login.this,"Login Sucsessfully", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                    });

                        }else{
                            Toast.makeText(Login.this, "Error!!" + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            loading(false);
                        }
                    }
                });
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Register.class));
            }
        });

        mPasswordResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), password_reset.class));
            }
        });

    }

    private void loading(boolean isloading){
        if (isloading){
            mLoginBtn.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mLoginBtn.setVisibility(View.VISIBLE);
        }
    }
}