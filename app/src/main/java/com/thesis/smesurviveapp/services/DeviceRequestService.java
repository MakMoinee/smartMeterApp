package com.thesis.smesurviveapp.services;

import android.content.Context;

import com.github.MakMoinee.library.interfaces.DefaultBaseListener;
import com.github.MakMoinee.library.interfaces.LocalVolleyRequestListener;
import com.github.MakMoinee.library.models.LocalVolleyRequestBody;
import com.github.MakMoinee.library.services.LocalVolleyRequest;

import org.json.JSONObject;

public class DeviceRequestService extends LocalVolleyRequest {
    public DeviceRequestService(Context mContext) {
        super(mContext);
    }


    public void pingDevice(String ip, DefaultBaseListener listener) {
        LocalVolleyRequestBody body = new LocalVolleyRequestBody.LocalVolleyRequestBodyBuilder()
                .setUrl(String.format("http://%s/", ip))
                .build();
        this.sendTextPlainRequest(body, new LocalVolleyRequestListener() {
            @Override
            public void onSuccessString(String response) {
                listener.onSuccess("success");
            }

            @Override
            public void onError(Error error) {
                listener.onError(error);
            }
        });
    }

    public void turnOffDevice(String ip, DefaultBaseListener listener) {
        LocalVolleyRequestBody body = new LocalVolleyRequestBody.LocalVolleyRequestBodyBuilder()
                .setUrl(String.format("http://%s/relay/off", ip))
                .build();
        this.sendTextPlainRequest(body, new LocalVolleyRequestListener() {
            @Override
            public void onSuccessString(String str) {
                listener.onSuccess("success");
            }

            @Override
            public void onError(Error error) {
                listener.onError(error);
            }
        });
    }

    public void turnOnDevice(String ip, DefaultBaseListener listener) {
        LocalVolleyRequestBody body = new LocalVolleyRequestBody.LocalVolleyRequestBodyBuilder()
                .setUrl(String.format("http://%s/relay/on", ip))
                .build();
        this.sendTextPlainRequest(body, new LocalVolleyRequestListener() {
            @Override
            public void onSuccessString(String str) {
                listener.onSuccess("success");
            }

            @Override
            public void onError(Error error) {
                listener.onError(error);
            }
        });
    }

    public void getDeviceVoltage(String ip, DefaultBaseListener listener) {
        LocalVolleyRequestBody body = new LocalVolleyRequestBody.LocalVolleyRequestBodyBuilder()
                .setUrl(String.format("http://%s/relay/voltage", ip))
                .build();
        this.sendJSONGetRequest(body, new LocalVolleyRequestListener() {

            @Override
            public void onSuccessJSON(JSONObject object) {
                listener.onSuccess(object);
            }

            @Override
            public void onError(Error error) {
                listener.onError(error);
            }
        });
    }
}
