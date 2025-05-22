package com.thesis.smesurviveapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.MakMoinee.library.interfaces.DefaultBaseListener;
import com.thesis.smesurviveapp.R;
import com.thesis.smesurviveapp.models.Consumptions;
import com.thesis.smesurviveapp.models.Devices;
import com.thesis.smesurviveapp.services.DeviceDB;

import java.util.List;

public class ConsumptionAdapter extends RecyclerView.Adapter<ConsumptionAdapter.ViewHolder> {
    Context mContext;
    List<Consumptions> consumptionsList;
    DeviceDB deviceDB;


    public ConsumptionAdapter(Context mContext, List<Consumptions> consumptionsList) {
        this.mContext = mContext;
        this.consumptionsList = consumptionsList;
        deviceDB = new DeviceDB(mContext);
    }

    @NonNull
    @Override
    public ConsumptionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_consumption, parent, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsumptionAdapter.ViewHolder holder, int position) {
        Consumptions c = consumptionsList.get(position);
        deviceDB.fetchDeviceById(c.getDeviceID(), new DefaultBaseListener() {
            @Override
            public <T> void onSuccess(T any) {
                if (any instanceof Devices) {
                    Devices devices = (Devices) any;
                    if (devices != null) {
                        holder.txtDeviceName.setText(devices.getDeviceName());
                        holder.txtEnergy.setText(String.format("%.2f", c.getConsumption()));
                    }
                }
            }

            @Override
            public void onError(Error error) {
                holder.itemView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return consumptionsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDeviceName, txtEnergy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDeviceName = itemView.findViewById(R.id.txtDeviceName);
            txtEnergy = itemView.findViewById(R.id.txtEnergy);
        }
    }
}
