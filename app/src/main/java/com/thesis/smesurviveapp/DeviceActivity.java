package com.thesis.smesurviveapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.MakMoinee.library.dialogs.MyDialog;
import com.github.MakMoinee.library.interfaces.DefaultBaseListener;
import com.google.gson.Gson;
import com.thesis.smesurviveapp.commons.Utils;
import com.thesis.smesurviveapp.databinding.ActivityDevicesBinding;
import com.thesis.smesurviveapp.databinding.DialogSetConsumptionBinding;
import com.thesis.smesurviveapp.databinding.DialogSetVoltageBinding;
import com.thesis.smesurviveapp.models.Consumptions;
import com.thesis.smesurviveapp.models.Devices;
import com.thesis.smesurviveapp.preference.DeviceSettingsPref;
import com.thesis.smesurviveapp.services.DeviceConsumption;
import com.thesis.smesurviveapp.services.DeviceDB;
import com.thesis.smesurviveapp.services.DeviceRequestService;
import com.thesis.smesurviveapp.services.MonitoringService;

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

    AlertDialog dialogConsumption;
    AlertDialog dialogVoltage;

    DialogSetConsumptionBinding consumptionBinding;

    DialogSetVoltageBinding voltageBinding;
    DeviceConsumption deviceConsumption;


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
        deviceConsumption = new DeviceConsumption(DeviceActivity.this);

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
                            voltage = obj.getDouble("voltage");
                            current = powerWatts / voltage;

                            double simulatedPowerFluctuation = (Math.random() - 0.5) * 0.5;
                            double power = current * voltage + simulatedPowerFluctuation;

                            Log.e("voltage",Double.toString(voltage));

                            if (Utils.isVoltageControlled) {
                                float mv = new DeviceSettingsPref(DeviceActivity.this).getVoltage();
                                if (voltage > mv) {
                                    myDialog.setCustomMessage("Voltage Exceeded, Turning it off ...");
                                    myDialog.show();
                                    requestService.turnOffDevice(selectedDevice.getDeviceIP(), new DefaultBaseListener() {
                                        @Override
                                        public <T> void onSuccess(T any) {
                                            myDialog.dismiss();
                                            myDialog = new MyDialog(DeviceActivity.this);
                                            Utils.isTurnedOn = false;
                                            Utils.rawDevice = "";
                                            Toast.makeText(DeviceActivity.this, "Successfully Turned Off Device Meter", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(Error error) {
                                            myDialog.dismiss();
                                            myDialog = new MyDialog(DeviceActivity.this);
                                            Toast.makeText(DeviceActivity.this, "Failed To Turn Off Device Meter, Please check network or ip and try again", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }
                            }

                            if (Utils.isTurnedOn) {

                                double simulatedElapseTime = 1.0 / 60.0;
                                energyWh += power * simulatedElapseTime;
                                energyKWh = energyWh / 1000.0;

                                if (Utils.isConsumptionControlled) {
                                    float myKWH = new DeviceSettingsPref(DeviceActivity.this).getConsumption();
                                    if (energyKWh > myKWH) {

                                        myDialog.setCustomMessage("Energy Consumption Exceeded, Turning it off ...");
                                        myDialog.show();
                                        requestService.turnOffDevice(selectedDevice.getDeviceIP(), new DefaultBaseListener() {
                                            @Override
                                            public <T> void onSuccess(T any) {
                                                myDialog.dismiss();
                                                myDialog = new MyDialog(DeviceActivity.this);
                                                Utils.isTurnedOn = false;
                                                Utils.rawDevice = "";
                                                Toast.makeText(DeviceActivity.this, "Successfully Turned Off Device Meter", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onError(Error error) {
                                                myDialog.dismiss();
                                                myDialog = new MyDialog(DeviceActivity.this);
                                                Toast.makeText(DeviceActivity.this, "Failed To Turn Off Device Meter, Please check network or ip and try again", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        return;
                                    }
                                }


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Consumptions c = new Consumptions.ConsumptionBuilder()
                                                .setConsumption(energyKWh)
                                                .setDeviceID(selectedDevice.getDeviceID())
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
                                        binding.txtPower.setText(String.format("%.2f", power));
                                        binding.txtVoltage.setText(String.format("%.2f", voltage));
                                        binding.txtConsumption.setText(String.format("%.2f kwh", energyKWh));
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.txtPower.setText(String.format("%.2f", power));
                                        binding.txtVoltage.setText(String.format("%.2f", voltage));
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
                    myDialog = new MyDialog(DeviceActivity.this);
                    Utils.isTurnedOn = false;
                    Utils.rawDevice = "";
//                    Intent stopIntent = new Intent(DeviceActivity.this, MonitoringService.class);
//                    stopService(stopIntent);
                    Toast.makeText(DeviceActivity.this, "Successfully Turned Off Device Meter", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Error error) {
                    myDialog.dismiss();
                    myDialog = new MyDialog(DeviceActivity.this);
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
//                    Intent serviceIntent = new Intent(DeviceActivity.this, MonitoringService.class);
                    Utils.rawDevice = new Gson().toJson(selectedDevice);
//                    ContextCompat.startForegroundService(DeviceActivity.this, serviceIntent);

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
                    Intent stopIntent = new Intent(DeviceActivity.this, MonitoringService.class);
                    stopService(stopIntent);

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

        binding.btnSetConsumption.setOnClickListener(v -> {
            consumptionBinding = DialogSetConsumptionBinding.inflate(LayoutInflater.from(DeviceActivity.this), null, false);
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(DeviceActivity.this);
            mBuilder.setView(consumptionBinding.getRoot());
            mBuilder.setCancelable(false);
            setConsumptionDialogListeners();
            dialogConsumption = mBuilder.create();
            dialogConsumption.show();
        });

        binding.btnSetVoltage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voltageBinding = DialogSetVoltageBinding.inflate(LayoutInflater.from(DeviceActivity.this), null, false);
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(DeviceActivity.this);
                mBuilder.setView(voltageBinding.getRoot());
                mBuilder.setCancelable(false);
                setVoltageDialogListeners();
                dialogVoltage = mBuilder.create();
                dialogVoltage.show();
            }
        });
    }

    private void setVoltageDialogListeners() {
        voltageBinding.btnSetVoltage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String volt = voltageBinding.editVolt.getText().toString().trim();
                if (volt.equals("")) {
                    Toast.makeText(DeviceActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
                } else {
                    float voltage = Float.parseFloat(volt);
                    new DeviceSettingsPref(DeviceActivity.this).storeVoltage(voltage);
                    if (voltage != 0) {
                        Toast.makeText(DeviceActivity.this, "Successfully Set Voltage", Toast.LENGTH_SHORT).show();
                        Utils.isVoltageControlled = true;
                        dialogVoltage.dismiss();
                    } else {
                        Toast.makeText(DeviceActivity.this, "Voltage Set To Zero.. Setting No Cap On Voltage", Toast.LENGTH_SHORT).show();
                        Utils.isVoltageControlled = false;
                        dialogVoltage.dismiss();
                    }
                }
            }
        });

        voltageBinding.btnCancel.setOnClickListener(v -> dialogVoltage.dismiss());
    }

    private void setConsumptionDialogListeners() {
        consumptionBinding.btnSetConsumption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String kwh = consumptionBinding.editKwh.getText().toString().trim();
                if (kwh.equals("")) {
                    Toast.makeText(DeviceActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
                } else {
                    float energy = Float.parseFloat(kwh);
                    new DeviceSettingsPref(DeviceActivity.this).storeConsumption(energy);
                    if (energy != 0) {
                        Toast.makeText(DeviceActivity.this, "Successfully Set Consumption", Toast.LENGTH_SHORT).show();
                        Utils.isConsumptionControlled = true;
                        dialogConsumption.dismiss();
                    } else {
                        Toast.makeText(DeviceActivity.this, "Consumption Set To Zero.. Setting No Cap On Consumption", Toast.LENGTH_SHORT).show();
                        Utils.isConsumptionControlled = false;
                        dialogConsumption.dismiss();
                    }
                }
            }
        });
        consumptionBinding.btnCancel.setOnClickListener(v -> dialogConsumption.dismiss());
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
