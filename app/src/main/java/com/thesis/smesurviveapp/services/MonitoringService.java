package com.thesis.smesurviveapp.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.github.MakMoinee.library.interfaces.DefaultBaseListener;
import com.google.gson.Gson;
import com.thesis.smesurviveapp.R;
import com.thesis.smesurviveapp.commons.Utils;
import com.thesis.smesurviveapp.models.Consumptions;
import com.thesis.smesurviveapp.models.Devices;
import com.thesis.smesurviveapp.preference.DeviceSettingsPref;

import org.json.JSONException;
import org.json.JSONObject;

public class MonitoringService extends Service {
    private Handler handler = new Handler();
    private Runnable monitorRunnable;
    private DeviceRequestService requestService;
    private Devices device;
    private double powerWatts = 100.0;
    private double energyWh = 0.0;

    private DeviceConsumption deviceConsumption;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String deviceJson = Utils.rawDevice;
        if (!deviceJson.equals("")) {
            device = new Gson().fromJson(deviceJson, Devices.class);
        }
        requestService = new DeviceRequestService(this);
        deviceConsumption = new DeviceConsumption(this);

        startForeground(1, createNotification()); // required for foreground services

        monitorRunnable = new Runnable() {
            @Override
            public void run() {
                if (!Utils.rawDevice.equals("")) {
                    device = new Gson().fromJson(deviceJson, Devices.class);
                    monitorEnergy();
                } else {
                    Log.e("empty_bckgrnd", "true");
                }
                handler.postDelayed(this, 30000); // every 30 seconds
            }
        };

        handler.post(monitorRunnable);
        return START_STICKY;
    }

    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel(
                "monitor_channel",
                "Energy Monitor",
                NotificationManager.IMPORTANCE_LOW
        );
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, "monitor_channel")
                .setContentTitle("Monitoring Energy Consumption")
                .setContentText("Running in background")
                .setSmallIcon(R.drawable.ic_currrent)
                .build();
    }

    private void monitorEnergy() {
        requestService.getDeviceVoltage(device.getDeviceIP(), new DefaultBaseListener() {
            @Override
            public <T> void onSuccess(T any) {
                if (any instanceof JSONObject obj) {
                    try {
                        double voltage = obj.getDouble("voltage");
                        double current = powerWatts / voltage;
                        double power = current * voltage + (Math.random() - 0.5) * 0.5;
                        double elapsedTime = 1.0 / 60.0;
                        energyWh += power * elapsedTime;

                        double energyKWh = energyWh / 1000.0;

                        float cap = new DeviceSettingsPref(MonitoringService.this).getConsumption();
                        if (Utils.isConsumptionControlled && energyKWh > cap) {
                            requestService.turnOffDevice(device.getDeviceIP(), new DefaultBaseListener() {
                                @Override
                                public <T> void onSuccess(T any) {
                                }

                                @Override
                                public void onError(Error error) {
                                }
                            });
                            return;
                        }

                        if (Utils.isTurnedOn) {
                            Consumptions c = new Consumptions.ConsumptionBuilder()
                                    .setConsumption(energyKWh)
                                    .setDeviceID(device.getDeviceID())
                                    .build();
                            deviceConsumption.upsertData(c, new DefaultBaseListener() {
                                @Override
                                public <T> void onSuccess(T any) {

                                    Log.e("success_mon", "success update");
                                }

                                @Override
                                public void onError(Error error) {
                                    Log.e("background_err", error.getLocalizedMessage());
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(Error error) {
            }
        });
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(monitorRunnable);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
