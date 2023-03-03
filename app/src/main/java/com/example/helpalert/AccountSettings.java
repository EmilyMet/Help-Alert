package com.example.helpalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountSettings extends AppCompatActivity {
    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        navigationView = findViewById(R.id.navigation);
        navigationView.setSelectedItemId(R.id.account);
        navigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.buttonTrack:
                        // Handle the home button click
                        startActivity(new Intent(AccountSettings.this, MainActivity.class));
                        return true;
                    case R.id.mapTrack:
                        // Handle the dashboard button click
                        startActivity(new Intent(AccountSettings.this, MapTracking.class));
                        return true;
                    case R.id.account:
                        // Handle the notifications button click
                        startActivity(new Intent(AccountSettings.this, AccountSettings.class));
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putInt("selectedItemId", navigationView.getSelectedItemId());
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        int selectedItemId = savedInstanceState.getInt("selectedItemId", R.id.account);
//        navigationView.setSelectedItemId(selectedItemId);
//    }


}