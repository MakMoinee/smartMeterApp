package com.thesis.smesurviveapp.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class DeviceSettingsPref {
    Context mContext;
    SharedPreferences pref;

    public DeviceSettingsPref(Context mContext) {
        this.mContext = mContext;
        this.pref = mContext.getSharedPreferences("deviceset", Context.MODE_PRIVATE);
    }


    public void storeConsumption(float consumption) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat("consumption", consumption);
        editor.commit();
        editor.apply();
    }

    public float getConsumption() {
        return this.pref.getFloat("consumption", 0);
    }

    public void storeVoltage(float voltage) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat("voltage", voltage);
        editor.commit();
        editor.apply();
    }

    public float getVoltage() {
        return this.pref.getFloat("voltage", 0);
    }
}
