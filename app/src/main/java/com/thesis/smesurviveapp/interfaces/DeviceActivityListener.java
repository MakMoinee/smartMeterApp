package com.thesis.smesurviveapp.interfaces;

import com.thesis.smesurviveapp.models.Devices;

public interface DeviceActivityListener {

    void onDeviceActivity(Devices devices);
    void onLogout();
}
