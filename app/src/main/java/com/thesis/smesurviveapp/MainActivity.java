package com.thesis.smesurviveapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.preference.LoginPref;
import com.thesis.smesurviveapp.databinding.ActivityMainBinding;
import com.thesis.smesurviveapp.models.Users;
import com.thesis.smesurviveapp.services.UserService;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userService = new UserService(MainActivity.this);
        setListeners();

        int userID = new LoginPref(MainActivity.this).getIntItem("userID");
        if (userID != 0) {
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void setListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.editUsername.getText().toString().trim();
            String password = binding.editPassword.getText().toString().trim();
            if (username.equals("") || password.equals("")) {
                Toast.makeText(MainActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            } else {
                Users users = userService.getUserRecord(username, password);
                if (users != null) {
                    new LoginPref(MainActivity.this).storeLogin(MapForm.convertObjectToMap(users));
                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Wrong username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });
    }
}