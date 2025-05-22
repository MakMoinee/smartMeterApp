package com.thesis.smesurviveapp.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Consumptions {
    private int id;
    private int deviceID;
    private double consumption;

    public Consumptions(ConsumptionBuilder builder) {
        this.id = builder.id;
        this.deviceID = builder.deviceID;
        this.consumption = builder.consumption;
    }

    public static class ConsumptionBuilder{
        private int id;
        private int deviceID;
        private double consumption;

        public ConsumptionBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public ConsumptionBuilder setDeviceID(int deviceID) {
            this.deviceID = deviceID;
            return this;
        }

        public ConsumptionBuilder setConsumption(double consumption) {
            this.consumption = consumption;
            return this;
        }

        public Consumptions build(){
            return new Consumptions(this);
        }
    }
}
