package com.thesis.smesurviveapp.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.MakMoinee.library.interfaces.DefaultBaseListener;
import com.github.MakMoinee.library.preference.LoginPref;
import com.thesis.smesurviveapp.adapters.ConsumptionAdapter;
import com.thesis.smesurviveapp.databinding.FragmentConsumptionHistoryBinding;
import com.thesis.smesurviveapp.models.Consumptions;
import com.thesis.smesurviveapp.models.Devices;
import com.thesis.smesurviveapp.services.DeviceConsumption;
import com.thesis.smesurviveapp.services.DeviceDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConsumptionHistory extends Fragment {

    FragmentConsumptionHistoryBinding binding;
    DeviceConsumption deviceConsumption;
    DeviceDB deviceDB;
    List<Consumptions> consumptionsList = new ArrayList<>();
    int uid = 0;
    ConsumptionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConsumptionHistoryBinding.inflate(LayoutInflater.from(requireContext()), container, false);
        deviceConsumption = new DeviceConsumption(requireContext());
        deviceDB = new DeviceDB(requireContext());
        uid = new LoginPref(requireContext()).getIntItem("userID");
        setListeners();
        loadData();

        return binding.getRoot();
    }

    private void setListeners() {
        binding.swipe.setOnRefreshListener(() -> {
            consumptionsList = new ArrayList<>();
            binding.recycler.setAdapter(null);
            loadData();
        });
    }

    private void loadData() {

        deviceDB.fetchDevices(uid, new DefaultBaseListener() {
            @Override
            public <T> void onSuccess(T any) {
                if (any instanceof List<?>) {
                    List<Devices> devicesList = (List<Devices>) any;
                    if (devicesList != null) {
                        deviceConsumption.fetchReportByDevice(new DefaultBaseListener() {
                            @Override
                            public <T> void onSuccess(T any) {
                                if (any instanceof HashMap<?, ?>) {
                                    HashMap<Integer, Double> map = (HashMap<Integer, Double>) any;
                                    if (map != null) {
                                        for (Devices devices : devicesList) {
                                            if (map.containsKey(devices.getDeviceID())) {
                                                Consumptions c = new Consumptions.ConsumptionBuilder()
                                                        .setDeviceID(devices.getDeviceID())
                                                        .setConsumption(map.get(devices.getDeviceID()))
                                                        .build();
                                                consumptionsList.add(c);
                                            }
                                        }

                                        if (consumptionsList.size() > 0) {
                                            adapter = new ConsumptionAdapter(requireContext(), consumptionsList);
                                            binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
                                            binding.recycler.setAdapter(adapter);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onError(Error error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onError(Error error) {

            }
        });


    }
}
