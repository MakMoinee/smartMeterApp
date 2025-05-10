package com.thesis.smesurviveapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.MakMoinee.library.dialogs.MyDialog;
import com.github.MakMoinee.library.interfaces.DefaultBaseListener;
import com.github.MakMoinee.library.services.HashPass;
import com.thesis.smesurviveapp.databinding.ActivityCreateAccountBinding;
import com.thesis.smesurviveapp.models.Users;
import com.thesis.smesurviveapp.services.UserService;

public class CreateAccountActivity extends AppCompatActivity {

    ActivityCreateAccountBinding binding;
    private UserService userService;
    private MyDialog myDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userService = new UserService(CreateAccountActivity.this);
        myDialog = new MyDialog(CreateAccountActivity.this);
        setListeners();
    }

    private void setListeners() {
        binding.btnSave.setOnClickListener(v -> {
            String username = binding.editUsername.getText().toString().trim();
            String password = binding.editPassword.getText().toString().trim();
            String confirmPass = binding.editConfirmPassword.getText().toString().trim();

            if (username.equals("") || password.equals("") || confirmPass.equals("")) {
                Toast.makeText(CreateAccountActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            } else {
                if (confirmPass.equals(password)) {
                    myDialog.show();
                    Users users = new Users.UserBuilder()
                            .setUsername(username)
                            .setPassword(new HashPass().makeHashPassword(password))
                            .setUserType("user")
                            .build();

                    userService.insertUniqueUser(users, new DefaultBaseListener() {
                        @Override
                        public <T> void onSuccess(T any) {
                            myDialog.dismiss();
                            Toast.makeText(CreateAccountActivity.this, "Successfully Created Account", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(Error error) {
                            myDialog.dismiss();
                            Toast.makeText(CreateAccountActivity.this, "Failed To Add Account, Please Try Again Later", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(CreateAccountActivity.this, "Password Doesn't match, Please Try Again Later", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
