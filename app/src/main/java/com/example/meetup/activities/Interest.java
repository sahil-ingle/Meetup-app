package com.example.meetup.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.meetup.databinding.ActivityInterestBinding;
import com.example.meetup.utilities.Constants;


public class Interest extends AppCompatActivity {

    private ActivityInterestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInterestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.Backbutton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));
        onClicklistener();

    }
    private void onClicklistener(){
        binding.Chess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.Interest = "Chess";
                startActivity(new Intent(getApplicationContext(), UsersActivity.class));
            }
        });
        binding.Coding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.Interest = "Coding";
                startActivity(new Intent(getApplicationContext(), UsersActivity.class));
            }
        });
        binding.Cricket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.Interest = "Cricket";
                startActivity(new Intent(getApplicationContext(), UsersActivity.class));
            }
        });
        binding.Football.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.Interest = "Football";
                startActivity(new Intent(getApplicationContext(), UsersActivity.class));
            }
        });
        binding.Painting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.Interest = "Painting";
                startActivity(new Intent(getApplicationContext(), UsersActivity.class));
            }
        });
        binding.Movie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.Interest = "Movie";
                startActivity(new Intent(getApplicationContext(), UsersActivity.class));
            }
        });
    }
}