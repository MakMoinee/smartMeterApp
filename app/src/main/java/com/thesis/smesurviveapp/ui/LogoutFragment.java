package com.thesis.smesurviveapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.thesis.smesurviveapp.interfaces.DeviceActivityListener;

public class LogoutFragment extends Fragment {

    DeviceActivityListener deviceActivityListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        deviceActivityListener.onLogout();
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DeviceActivityListener) {
            deviceActivityListener = (DeviceActivityListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement DeviceActivityListener");
        }
    }
}
