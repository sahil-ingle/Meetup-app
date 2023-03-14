package com.example.meetup.activities;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.meetup.adapters.UsersAdapter;
import com.example.meetup.databinding.ActivityUsersBinding;
import com.example.meetup.listenrs.UserListener;
import com.example.meetup.models.User;
import com.example.meetup.utilities.Constants;
import com.example.meetup.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    private void setListeners(){
        binding.backImg.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                //.whereEqualTo(Constants.KEY_INTEREST, Constants.Interest)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.img = queryDocumentSnapshot.getString(Constants.KEY_IMG);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);

                        }
                        if (users.size() > 0){
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.userRecyclerview.setAdapter(usersAdapter);
                            binding.userRecyclerview.setVisibility(View.VISIBLE);
                        }else {
                            showErrorMsg();
                        }
                    }else {
                        showErrorMsg();
                    }
                });
    }

    private void showErrorMsg(){
        binding.textErrorMsg.setText(String.format("%s", "No user available"));
        binding.textErrorMsg.setVisibility(View.VISIBLE);
    }


    private void loading(Boolean isLoading){
        if (isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        } else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}