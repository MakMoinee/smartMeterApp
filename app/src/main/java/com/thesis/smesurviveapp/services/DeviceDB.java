package com.thesis.smesurviveapp.services;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.DefaultBaseListener;
import com.thesis.smesurviveapp.models.Devices;
import com.thesis.smesurviveapp.repository.LocalSQLite;

import java.util.ArrayList;
import java.util.List;

public class DeviceDB {
    private LocalSQLite sqLite;
    private final String TABLE_DEVICES = "devices";

    public DeviceDB(Context mContext) {
        sqLite = LocalSQLite.getInstance(mContext);
    }

    public void insertDevice(Devices devices, DefaultBaseListener listener) {
        SQLiteDatabase db = sqLite.getWritableDatabase();
        ContentValues values = MapForm.toContentValues(devices);
        values.remove("deviceID");
        try {
            long count = db.insert(TABLE_DEVICES, null, values);
            if (count != -1) {
                listener.onSuccess("success add");
            } else {
                listener.onError(new Error("failed to add error"));
            }
        } catch (Exception e) {
            Log.e("error_insert", e.getLocalizedMessage());
            listener.onError(new Error(e.getMessage()));
        } finally {
            db.close();
        }
    }

    public void insertUniqueDevice(Devices devices, DefaultBaseListener listener) {
        if (devices == null) {
            listener.onError(new Error("empty devices"));
        } else {
            this.checkDevice(devices, new DefaultBaseListener() {
                @Override
                public <T> void onSuccess(T any) {
                    listener.onError(new Error("device ip exist"));
                }

                @Override
                public void onError(Error error) {
                    insertDevice(devices, new DefaultBaseListener() {
                        @Override
                        public <T> void onSuccess(T any) {
                            listener.onSuccess("success add");
                        }

                        @Override
                        public void onError(Error error) {
                            listener.onError(error);
                        }
                    });
                }
            });
        }
    }

    @SuppressLint("Range")
    private void checkDevice(Devices devices, DefaultBaseListener listener) {
        SQLiteDatabase db = sqLite.getReadableDatabase();
        String[] columns = {"deviceName", "deviceIP"};
        String selection = "deviceIP=? AND userID=?";
        String[] selectionArgs = {
                devices.getDeviceIP(),
                String.valueOf(devices.getUserID())
        };

        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_DEVICES, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                listener.onSuccess("device exists");
            } else {
                listener.onError(new Error("Device not found for this user."));
            }

        } catch (Exception e) {
            listener.onError(new Error("Error checking device: " + e.getMessage()));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }



    @SuppressLint("Range")
    public void fetchDevices(int userID, DefaultBaseListener listener) {
        SQLiteDatabase db = sqLite.getReadableDatabase();
        List<Devices> deviceList = new ArrayList<>();
        Cursor cursor = null;

        try {
            String selection = "userID=?";
            String[] selectionArgs = {String.valueOf(userID)};

            cursor = db.query(TABLE_DEVICES, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Devices device = new Devices();
                    device.setDeviceID(cursor.getInt(cursor.getColumnIndex("deviceID")));
                    device.setUserID(cursor.getInt(cursor.getColumnIndex("userID")));
                    device.setDeviceName(cursor.getString(cursor.getColumnIndex("deviceName")));
                    device.setDeviceIP(cursor.getString(cursor.getColumnIndex("deviceIP")));
                    device.setStatus(cursor.getString(cursor.getColumnIndex("status")));
                    device.setRegisteredDate(cursor.getString(cursor.getColumnIndex("registeredDate")));
                    deviceList.add(device);
                } while (cursor.moveToNext());

                listener.onSuccess(deviceList); // Pass list back to listener
            } else {
                listener.onError(new Error("No devices found for this user."));
            }
        } catch (Exception e) {
            listener.onError(new Error("Error fetching devices: " + e.getMessage()));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public void deleteDevice(int deviceID, DefaultBaseListener listener) {
        SQLiteDatabase db = sqLite.getWritableDatabase();

        try {
            String whereClause = "deviceID=?";
            String[] whereArgs = {String.valueOf(deviceID)};

            int rowsDeleted = db.delete(TABLE_DEVICES, whereClause, whereArgs);

            if (rowsDeleted > 0) {
                listener.onSuccess("Device deleted successfully.");
            } else {
                listener.onError(new Error("Device not found."));
            }

        } catch (Exception e) {
            listener.onError(new Error("Error deleting device: " + e.getMessage()));
        } finally {
            db.close();
        }
    }



}
