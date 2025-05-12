package com.thesis.smesurviveapp.ui.devices;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.MakMoinee.library.dialogs.MyDialog;
import com.github.MakMoinee.library.interfaces.DefaultBaseListener;
import com.github.MakMoinee.library.preference.LoginPref;
import com.github.MakMoinee.library.services.Utils;
import com.thesis.smesurviveapp.adapters.DeviceAdapters;
import com.thesis.smesurviveapp.databinding.DialogAddDeviceBinding;
import com.thesis.smesurviveapp.databinding.FragmentDevicesBinding;
import com.thesis.smesurviveapp.interfaces.DeviceActivityListener;
import com.thesis.smesurviveapp.interfaces.DeviceListener;
import com.thesis.smesurviveapp.models.Devices;
import com.thesis.smesurviveapp.services.DeviceDB;
import com.thesis.smesurviveapp.services.DeviceRequestService;

import java.util.ArrayList;
import java.util.List;

public class DeviceFragment extends Fragment {

    FragmentDevicesBinding binding;
    MyDialog myDialog;

    DialogAddDeviceBinding dialogAddDeviceBinding;
    AlertDialog popUpAdd;

    DeviceRequestService requestService;

    DeviceDB deviceDB;
    String lastIP = "";
    DeviceAdapters adapter;

    List<Devices> devicesList = new ArrayList<>();
    int userID = 0;

    DeviceActivityListener deviceActivityListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDevicesBinding.inflate(inflater, container, false);
        myDialog = new MyDialog(requireContext());
        requestService = new DeviceRequestService(requireContext());
        deviceDB = new DeviceDB(requireContext());
        setListeners();
        loadData();
        return binding.getRoot();
    }

    private void loadData() {
        devicesList = new ArrayList<>();
        binding.recycler.setAdapter(null);
        userID = new LoginPref(requireContext()).getIntItem("userID");
        Log.e("userid>>>", Integer.toString(userID));
        deviceDB.fetchDevices(userID, new DefaultBaseListener() {
            @Override
            public <T> void onSuccess(T any) {
                if (any instanceof List<?>) {
                    List<Devices> dList = (List<Devices>) any;
                    if (dList != null && dList.size() > 0) {
                        devicesList = dList;

                        if (devicesList.size() > 0) {
                            adapter = new DeviceAdapters(requireContext(), devicesList, new DeviceListener() {
                                @Override
                                public void onClickListener() {

                                }

                                @Override
                                public void onDeleteClickListener(int position) {
                                    deleteDevice(devicesList.get(position));
                                }

                                @Override
                                public void onClickListener(int position) {
                                    openDeviceaActivity(devicesList.get(position));
                                }
                            });
                            binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
                            binding.recycler.setAdapter(adapter);
                        }
                    }
                }
            }

            @Override
            public void onError(Error error) {
                Log.e("error_load", error.getLocalizedMessage());
            }
        });
    }

    private void openDeviceaActivity(Devices devices) {
        if (devices != null) {
            deviceActivityListener.onDeviceActivity(devices);
        } else {
            Toast.makeText(requireContext(), "Can't Open Device Activity, Please Try Again Later", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteDevice(Devices devices) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(requireContext());
        DialogInterface.OnClickListener dListener = (dd, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE:
                    if (devices != null) {
                        deviceDB.deleteDevice(devices.getDeviceID(), new DefaultBaseListener() {
                            @Override
                            public <T> void onSuccess(T any) {
                                com.thesis.smesurviveapp.commons.Utils.isTurnedOn = false;
                                Toast.makeText(requireContext(), "Successfully Deleted Device", Toast.LENGTH_SHORT).show();
                                loadData();
                            }

                            @Override
                            public void onError(Error error) {
                                Toast.makeText(requireContext(), "Failed To Delete Device, Please Try Again Later", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    break;
                default:
                    dd.dismiss();
                    break;
            }
        };

        mBuilder.setMessage("Are You Sure You Want To Delete This Device?")
                .setNegativeButton("Yes", dListener)
                .setPositiveButton("No", dListener)
                .setCancelable(false)
                .show();
    }

    private void setListeners() {
        binding.btnAdd.setOnClickListener(v -> {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(requireContext());
            dialogAddDeviceBinding = DialogAddDeviceBinding.inflate(LayoutInflater.from(requireContext()), null, false);
            mBuilder.setView(dialogAddDeviceBinding.getRoot());
            mBuilder.setCancelable(false);
            setDialogListeners();
            popUpAdd = mBuilder.create();
            popUpAdd.show();
        });
    }

    private void setDialogListeners() {
        dialogAddDeviceBinding.btnPing.setOnClickListener(v -> {
            String ip = dialogAddDeviceBinding.editDeviceIP.getText().toString().trim();
            if (lastIP != ip) {
                dialogAddDeviceBinding.btnSave.setEnabled(false);
            }
            if (ip.equals("")) {
                Toast.makeText(requireContext(), "Please Don't Leave IP Field Empty", Toast.LENGTH_SHORT).show();
            } else {
                myDialog.show();
                requestService.pingDevice(ip, new DefaultBaseListener() {
                    @Override
                    public <T> void onSuccess(T any) {
                        myDialog.dismiss();
                        Toast.makeText(requireContext(), "Device is pingable, please press save to proceed ...", Toast.LENGTH_SHORT).show();
                        lastIP = ip;
                        dialogAddDeviceBinding.btnSave.setEnabled(true);
                    }

                    @Override
                    public void onError(Error error) {
                        myDialog.dismiss();
                        Toast.makeText(requireContext(), "Failed To Ping Device, Please Try Again Later", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialogAddDeviceBinding.btnSave.setOnClickListener(v -> {
            String deviceName = dialogAddDeviceBinding.editDeviceName.getText().toString().trim();
            String deviceIP = dialogAddDeviceBinding.editDeviceIP.getText().toString().trim();

            if (deviceName.equals("") || deviceIP.equals("")) {
                Toast.makeText(requireContext(), "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            } else {
                myDialog.show();
                Devices newDevice = new Devices.DeviceBuilder()
                        .setDeviceIP(deviceIP)
                        .setUserID(userID)
                        .setDeviceName(deviceName)
                        .setStatus("active")
                        .setRegisteredDate(Utils.getCurrentDate("yyyy-MM-dd"))
                        .build();

                deviceDB.insertUniqueDevice(newDevice, new DefaultBaseListener() {
                    @Override
                    public <T> void onSuccess(T any) {
                        myDialog.dismiss();
                        Toast.makeText(requireContext(), "Successfully Added Device", Toast.LENGTH_SHORT).show();
                        loadData();
                        popUpAdd.dismiss();
                    }

                    @Override
                    public void onError(Error error) {
                        myDialog.dismiss();
                        Toast.makeText(requireContext(), "Failed To Add Device, Please Try Again Later", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialogAddDeviceBinding.btnCancel.setOnClickListener(v -> {
            dialogAddDeviceBinding.editDeviceIP.setText("");
            dialogAddDeviceBinding.editDeviceName.setText("");
            popUpAdd.dismiss();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        dialogAddDeviceBinding = null;
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
