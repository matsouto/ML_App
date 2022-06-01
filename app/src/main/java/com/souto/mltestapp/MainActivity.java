package com.souto.mltestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.souto.mltestapp.Auth.Login;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN); // Hide Status
        setContentView(R.layout.activity_main);

        // Setting the callback for the planner button
        mAuth = FirebaseAuth.getInstance();

        // Get the logged user, if it exists
        FirebaseUser current_user = mAuth.getCurrentUser();

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Check if user is logged in
            if(current_user == null) {
                Intent intent_login = new Intent(MainActivity.this, Login.class);
                startActivity(intent_login);
                finish();
            }else{
                Intent intent_train = new Intent(MainActivity.this, Train.class);
                startActivity(intent_train);
                finish();
            }
        }, 3500);



    }
}