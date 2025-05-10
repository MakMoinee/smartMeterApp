package com.thesis.smesurviveapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thesis.smesurviveapp.R;
import com.thesis.smesurviveapp.interfaces.DeviceListener;
import com.thesis.smesurviveapp.models.Devices;

import java.util.List;

public class DeviceAdapters extends RecyclerView.Adapter<DeviceAdapters.ViewHolder> {
    Context mContext;
    List<Devices> devicesList;

    DeviceListener listener;

    public DeviceAdapters(Context mContext, List<Devices> devicesList, DeviceListener listener) {
        this.mContext = mContext;
        this.devicesList = devicesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceAdapters.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_device, null, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapters.ViewHolder holder, int position) {
        Devices devices = devicesList.get(position);
        holder.txtDeviceName.setText(String.format("%s-%s", devices.getDeviceName(), devices.getDeviceIP()));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClickListener(holder.getAdapterPosition()));
        holder.itemView.setOnClickListener(v -> listener.onClickListener(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return devicesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDeviceName;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDeviceName = itemView.findViewById(R.id.txtDeviceName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
