package com.thesis.smesurviveapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.MakMoinee.library.preference.LoginPref;
import com.github.MakMoinee.library.services.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.thesis.smesurviveapp.databinding.ActivityDashboardBinding;
import com.thesis.smesurviveapp.interfaces.DeviceActivityListener;
import com.thesis.smesurviveapp.models.Devices;

import org.w3c.dom.Text;

public class DashboardActivity extends AppCompatActivity implements DeviceActivityListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDashboardBinding binding;
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarDashboard.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        View mView = navigationView.getHeaderView(0);
        TextView txtUser = mView.findViewById(R.id.txtUser);
        String username = new LoginPref(DashboardActivity.this).getStringItem("username");
        txtUser.setText(username);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_devices, R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_dashboard);
        Utils.setUpNavigation(this, navigationView, navController, mAppBarConfiguration);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_dashboard);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onDeviceActivity(Devices devices) {
        navController.navigate(R.id.nav_home);
        Intent intent = new Intent(DashboardActivity.this, DeviceActivity.class);
        intent.putExtra("device", new Gson().toJson(devices));
        startActivity(intent);
    }

    @Override
    public void onLogout() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(DashboardActivity.this);
        DialogInterface.OnClickListener dListener = (dd, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE:
                    new LoginPref(DashboardActivity.this).clearLogin();
                    Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(DashboardActivity.this, "Logout Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                default:
                    navController.navigate(R.id.nav_home);
                    dd.dismiss();
                    break;
            }
        };
        mBuilder.setMessage("Are You Sure You Want To Logout?")
                .setNegativeButton("Yes", dListener)
                .setPositiveButton("No", dListener)
                .setCancelable(false)
                .show();
    }
}