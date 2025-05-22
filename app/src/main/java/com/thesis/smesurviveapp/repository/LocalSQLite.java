package com.thesis.smesurviveapp.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.MakMoinee.library.services.MSqliteDBHelper;

public class LocalSQLite extends MSqliteDBHelper {
    private static final String dbName = "survive.db";
    private static LocalSQLite instance;

    public LocalSQLite(Context context) {
        super(context, dbName);
    }

    public static LocalSQLite getInstance(Context context) {
        if (instance == null) {
            instance = new LocalSQLite(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "userID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL, " +
                "password TEXT NOT NULL, " +
                "userType TEXT NOT NULL)");

        db.execSQL("CREATE TABLE IF NOT EXISTS devices (" +
                "deviceID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userID INTEGER NOT NULL, " +
                "deviceName TEXT NOT NULL, " +
                "deviceIP TEXT NOT NULL, " +
                "status TEXT NOT NULL, " +
                "registeredDate TEXT NOT NULL)");

        db.execSQL("CREATE TABLE IF NOT EXISTS consumption (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "deviceID INTEGER NOT NULL, " +
                "consumption REAL NOT NULL)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS devices");
        db.execSQL("DROP TABLE IF EXISTS consumption");
        onCreate(db);
    }
}
