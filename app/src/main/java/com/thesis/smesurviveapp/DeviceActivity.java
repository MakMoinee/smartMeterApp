package com.thesis.smesurviveapp;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.MakMoinee.library.dialogs.MyDialog;
import com.github.MakMoinee.library.interfaces.DefaultBaseListener;
import com.google.gson.Gson;
import com.thesis.smesurviveapp.commons.Utils;
import com.thesis.smesurviveapp.databinding.ActivityDevicesBinding;
import com.thesis.smesurviveapp.models.Devices;
import com.thesis.smesurviveapp.services.DeviceDB;
import com.thesis.smesurviveapp.services.DeviceRequestService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DeviceActivity extends AppCompatActivity {

    ActivityDevicesBinding binding;
    Devices selectedDevice;

    DeviceRequestService requestService;

    DeviceDB deviceDB;
    MyDialog myDialog;

    double voltage = 0.0;
    double current = 0.0;
    final double powerWatts = 100.0;
    long elapsedTimeHours = 0;

    double energyWh = 0.0;

    double energyKWh = 0.0;

    private final Handler handler = new Handler();
    private final Runnable dataLoaderRunnable = new Runnable() {
        @Override
        public void run() {
            loadData();
            handler.postDelayed(this, 3000); // run again after 30 seconds
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(dataLoaderRunnable);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDevicesBinding.inflate(getLayoutInflater());
        requestService = new DeviceRequestService(DeviceActivity.this);
        deviceDB = new DeviceDB(DeviceActivity.this);
        myDialog = new MyDialog(DeviceActivity.this);

        String rawDevice = getIntent().getStringExtra("device");
        if (rawDevice != null) {
            try {
                selectedDevice = new Gson().fromJson(rawDevice, Devices.class);

            } catch (Exception e) {
                Log.e("error_intent", e.getLocalizedMessage());
            }

        } else {
            finish();
        }

        setListeners();
        setContentView(binding.getRoot());
    }

    private void loadData() {
        requestService.getDeviceVoltage(selectedDevice.getDeviceIP(), new DefaultBaseListener() {
            @Override
            public <T> void onSuccess(T any) {
                if (any instanceof JSONObject) {
                    JSONObject obj = (JSONObject) any;
                    if (obj != null) {
                        try {
                            voltage = obj.getDouble("volt");
                            current = powerWatts / voltage;

                            if (voltage == 0.0 || Double.isNaN(voltage) || Double.isInfinite(voltage)) {
                                voltage = 0.1; // fallback or show 0 safely in UI
                            }


                            double simulatedPowerFluctuation = (Math.random() - 0.5) * 0.5;
                            double power = current * voltage + simulatedPowerFluctuation;

                            Log.e("get3", Double.toString(voltage));
                            Log.e("get4", Double.toString(power));

                            if (Utils.isTurnedOn) {
                                double simulatedElapseTime = 1.0 / 60.0;
                                energyWh += power * simulatedElapseTime;
                                energyKWh = energyWh / 1000.0;

                                Log.e("get2", Double.toString(energyKWh));
                                Log.e("get3", Double.toString(voltage));

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.txtPower.speedTo((float) Math.round(power * 100) / 100);
                                        binding.txtVoltage.speedTo((float) Math.round(voltage * 100) / 100);
                                        binding.txtConsumption.setText(String.format("%.2f kwh", energyKWh));
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.txtPower.speedTo((float) Math.round(power * 100) / 100);
                                        binding.txtVoltage.speedTo((float) Math.round(voltage * 100) / 100);
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            voltage = 0;
                        }
                    }
                }
            }

            @Override
            public void onError(Error error) {
                Log.e("error", error.getLocalizedMessage());
            }
        });
    }

    private void setListeners() {

        binding.btnCheck.setOnClickListener(v -> {
            myDialog.show();
            requestService.pingDevice(selectedDevice.getDeviceIP(), new DefaultBaseListener() {
                @Override
                public <T> void onSuccess(T any) {
                    myDialog.dismiss();
                    Toast.makeText(DeviceActivity.this, "Device Meter is active", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Error error) {
                    myDialog.dismiss();
                    Toast.makeText(DeviceActivity.this, "Device Meter is inactive", Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnTurnOff.setOnClickListener(v -> {
            myDialog.show();
            requestService.turnOffDevice(selectedDevice.getDeviceIP(), new DefaultBaseListener() {
                @Override
                public <T> void onSuccess(T any) {
                    myDialog.dismiss();
                    Utils.isTurnedOn = false;
                    Toast.makeText(DeviceActivity.this, "Successfully Turned Off Device Meter", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Error error) {
                    myDialog.dismiss();
                    Toast.makeText(DeviceActivity.this, "Failed To Turn Off Device Meter, Please check network or ip and try again", Toast.LENGTH_SHORT).show();
                }
            });
        });


        binding.btnTurnOn.setOnClickListener(v -> {
            myDialog.show();
            requestService.turnOnDevice(selectedDevice.getDeviceIP(), new DefaultBaseListener() {
                @Override
                public <T> void onSuccess(T any) {
                    myDialog.dismiss();
                    Utils.isTurnedOn = true;
                    Toast.makeText(DeviceActivity.this, "Successfully Turned On Device Meter", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Error error) {
                    myDialog.dismiss();
                    Toast.makeText(DeviceActivity.this, "Failed To Turn On Device Meter, Please check network or ip and try again", Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnDelete.setOnClickListener(v -> {

            myDialog.show();
            deviceDB.deleteDevice(selectedDevice.getDeviceID(), new DefaultBaseListener() {
                @Override
                public <T> void onSuccess(T any) {
                    myDialog.dismiss();
                    Utils.isTurnedOn = false;
                    Toast.makeText(DeviceActivity.this, "Successfully Deleted Device", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Error error) {
                    myDialog.dismiss();
                    Toast.makeText(DeviceActivity.this, "Failed To Delete Device Meter, Please Try Again Later", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void getElapseTime() {
        String registeredDateStr = selectedDevice.getRegisteredDate(); // e.g. "2024-05-10 14:30:00"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date registeredDate = sdf.parse(registeredDateStr);
            long currentTimeMillis = System.currentTimeMillis();
            long registeredTimeMillis = registeredDate.getTime();

            long diffMillis = currentTimeMillis - registeredTimeMillis;
            elapsedTimeHours = TimeUnit.MILLISECONDS.toHours(diffMillis);

            Log.d("ElapsedTime", "Elapsed time in hours: " + elapsedTimeHours);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(dataLoaderRunnable);
    }
}
