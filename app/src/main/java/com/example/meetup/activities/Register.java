package com.example.meetup.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    EditText mEmail,mPasssword,mConfirmPass;
    Button mRegisterBtn;
    TextView mLoginBtn, mPasswordIsWeek,mConfPassNotMatch;
    FirebaseAuth firebaseAuth;
    ProgressBar mProgressBar;
    private PreferenceManager preferenceManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmail = findViewById(R.id.Email);
        mPasssword = findViewById(R.id.Password);
        mConfirmPass = findViewById(R.id.ConfirmPassword);
        mRegisterBtn = findViewById(R.id.Registerbtn);
        mLoginBtn = findViewById(R.id.AlreadyHaveAcc);
        mProgressBar =findViewById(R.id.progressBar);
        mPasswordIsWeek = findViewById(R.id.PasswordWeekMsg);
        mConfPassNotMatch = findViewById(R.id.PasswordNotmatched);

        firebaseAuth = firebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), profile.class));
            finish();
        }

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading(true);
                String email = mEmail.getText().toString().trim();
                String password = mPasssword.getText().toString().trim();
                String confirmpass = mConfirmPass.getText().toString().trim();
                preferenceManager = new PreferenceManager(getApplicationContext());

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
                if (!isValidPassword(password)){
                    mPasswordIsWeek.setVisibility(View.VISIBLE);
                    loading(false);
                    return;
                }
                if (!password.equals(confirmpass)){
                    mConfPassNotMatch.setVisibility(View.VISIBLE);
                    loading(false);
                    return;
                }

                mPasswordIsWeek.setVisibility(View.GONE);
                mConfPassNotMatch.setVisibility(View.GONE);

                firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            HashMap<String, Object> user = new HashMap<>();
                            user.put(Constants.KEY_EMAIL, mEmail.getText().toString().trim());
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .add(user)
                                    .addOnSuccessListener(documentReference -> {
                                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,false);
                                        preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());

                                        Toast.makeText(Register.this,"Register Sucsessfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), profile.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(exception -> {
                                        loading(false);
                                        Toast.makeText(getApplicationContext(), "Error in database", Toast.LENGTH_SHORT).show();
                                    });

                        }else {
                            loading(false);
                            Toast.makeText(Register.this, "Error!!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    private void loading(Boolean isLoading){
        if (isLoading){
            mRegisterBtn.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else{
            mProgressBar.setVisibility(View.INVISIBLE);
            mRegisterBtn.setVisibility(View.VISIBLE);
        }
    }


}