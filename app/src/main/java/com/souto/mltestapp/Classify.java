package com.souto.mltestapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class Classify extends AppCompatActivity {

    // Bottom nav bar
    public BottomNavigationView bottomNavigationView;
    public ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN); // Hide Status
        setContentView(R.layout.activity_classify);

        // Set up the navigation tab
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_Classify);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.page_Train:
                        startActivity(new Intent(Classify.this, Train.class));
                        finish();
//                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.page_Settings:
                        startActivity(new Intent(Classify.this, Settings.class));
                        finish();
//                        viewPager.setCurrentItem(2);
                        break;
                    default:
//                        viewPager.setCurrentItem(0);
                }
                return true;
            }
        });

    }
}