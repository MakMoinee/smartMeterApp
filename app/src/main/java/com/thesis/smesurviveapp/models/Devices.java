package com.thesis.smesurviveapp.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Devices {
    private int deviceID;
    private int userID;
    private String deviceName;
    private String deviceIP;
    private String status;
    private String registeredDate;

    public Devices(DeviceBuilder builder) {
        this.deviceID = builder.deviceID;
        this.userID = builder.userID;
        this.deviceName = builder.deviceName;
        this.deviceIP = builder.deviceIP;
        this.status = builder.status;
        this.registeredDate = builder.registeredDate;
    }

    public static class DeviceBuilder{

        private int deviceID;
        private int userID;
        private String deviceName;
        private String deviceIP;
        private String status;
        private String registeredDate;

        public DeviceBuilder setUserID(int userID) {
            this.userID = userID;
            return this;
        }

        public DeviceBuilder setDeviceID(int deviceID) {
            this.deviceID = deviceID;
            return this;
        }

        public DeviceBuilder setDeviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public DeviceBuilder setDeviceIP(String deviceIP) {
            this.deviceIP = deviceIP;
            return this;
        }

        public DeviceBuilder setStatus(String status) {
            this.status = status;
            return this;
        }

        public DeviceBuilder setRegisteredDate(String registeredDate) {
            this.registeredDate = registeredDate;
            return this;
        }

        public Devices build(){
            return new Devices(this);
        }
    }
}
