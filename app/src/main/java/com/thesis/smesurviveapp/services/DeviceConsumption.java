package com.thesis.smesurviveapp.services;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.DefaultBaseListener;
import com.thesis.smesurviveapp.models.Consumptions;
import com.thesis.smesurviveapp.repository.LocalSQLite;

import java.util.HashMap;

public class DeviceConsumption {
    private LocalSQLite sqLite;
    private final String TABLE_CONSUMPTION = "consumption";

    public DeviceConsumption(Context mContext) {
        sqLite = LocalSQLite.getInstance(mContext);
    }


    public void upsertData(Consumptions consumptions, DefaultBaseListener listener) {

        this.checkDevice(consumptions, new DefaultBaseListener() {
            @Override
            public <T> void onSuccess(T any) {
                if (any instanceof Consumptions) {
                    Consumptions c = (Consumptions) any;
                    if (c != null) {
                        c.setConsumption(consumptions.getConsumption());
                        update(c, new DefaultBaseListener() {
                            @Override
                            public <T> void onSuccess(T any) {
                                listener.onSuccess("success update");
                            }

                            @Override
                            public void onError(Error error) {
                                listener.onError(new Error("failed to update"));
                            }
                        });
                    } else {
                        listener.onError(new Error("empty"));
                    }
                }
            }

            @Override
            public void onError(Error error) {
                if (error.getLocalizedMessage().equals("Consumptions not found for this device.")) {
                    insert(consumptions, new DefaultBaseListener() {
                        @Override
                        public <T> void onSuccess(T any) {
                            listener.onSuccess("success");
                        }

                        @Override
                        public void onError(Error error) {
                            listener.onError(error);
                        }
                    });
                }
            }
        });

    }


    public void insert(Consumptions consumptions, DefaultBaseListener listener) {
        SQLiteDatabase db = sqLite.getWritableDatabase();
        ContentValues values = MapForm.toContentValues(consumptions);
        values.remove("id");
        try {
            long count = db.insert(TABLE_CONSUMPTION, null, values);
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

    @SuppressLint("Range")
    private void checkDevice(Consumptions consumptions, DefaultBaseListener listener) {
        SQLiteDatabase db = sqLite.getReadableDatabase();
        String[] columns = {"id", "userID", "deviceID"};
        String selection = "deviceID=?";
        String[] selectionArgs = {
                Integer.toString(consumptions.getDeviceID()),
        };

        Cursor cursor = null;
        int id = 0;
        try {
            cursor = db.query(TABLE_CONSUMPTION, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    id = cursor.getInt(cursor.getColumnIndex("id"));


                } while (cursor.moveToNext());

                if (id != 0) {
                    consumptions.setConsumption(id);
                    listener.onSuccess(consumptions);
                }
            } else {
                listener.onError(new Error("Consumptions not found for this device."));
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

    public void update(Consumptions consumptions, DefaultBaseListener listener) {
        SQLiteDatabase db = sqLite.getWritableDatabase();
        ContentValues values = MapForm.toContentValues(consumptions);

        // Extract the ID for the WHERE clause and remove it from values to avoid updating it
        String id = String.valueOf(consumptions.getId());
        values.remove("id");

        try {
            int rowsAffected = db.update(
                    TABLE_CONSUMPTION,
                    values,
                    "id = ?",
                    new String[]{id}
            );

            if (rowsAffected > 0) {
                listener.onSuccess("success update");
            } else {
                listener.onError(new Error("no rows updated"));
            }
        } catch (Exception e) {
            Log.e("error_update", e.getLocalizedMessage());
            listener.onError(new Error(e.getMessage()));
        } finally {
            db.close();
        }
    }

    @SuppressLint("Range")
    public void fetchReportByDevice(DefaultBaseListener listener) {
        SQLiteDatabase db = sqLite.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT deviceID, SUM(consumption) as totalConsumption FROM " + TABLE_CONSUMPTION + " GROUP BY deviceID";
            cursor = db.rawQuery(query, null);

            HashMap<Integer, Double> report = new HashMap<>();

            if (cursor.moveToFirst()) {
                do {
                    int deviceId = cursor.getInt(cursor.getColumnIndex("deviceID"));
                    double totalConsumption = cursor.getDouble(cursor.getColumnIndex("totalConsumption"));
                    report.put(deviceId, totalConsumption);
                } while (cursor.moveToNext());

                listener.onSuccess(report); // return the map
            } else {
                listener.onError(new Error("No consumption data found."));
            }

        } catch (Exception e) {
            Log.e("fetch_report_error", e.getLocalizedMessage());
            listener.onError(new Error("Error fetching report: " + e.getMessage()));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }


}
