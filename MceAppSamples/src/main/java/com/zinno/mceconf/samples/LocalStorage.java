package com.zinno.mceconf.samples;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by krzysztofwrobel on 02/02/15.
 */
public class LocalStorage {
    public static final String PREFS_NAME = "LocalStorage";
    public static final String PREF_DEVICE_MAP = "deviceMap";
    private static Gson gson;

    static {
        gson = new Gson();
    }

    public static void setAsDevice(Context context, BluetoothDevice bluetoothDevice, int deviceNumber) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        HashMap<Integer, BluetoothDevice> deviceMap = getDeviceMap(preferences);
        deviceMap.put(deviceNumber, bluetoothDevice);

        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(PREF_DEVICE_MAP, gson.toJson(deviceMap));
        edit.apply();
    }

    private static HashMap<Integer, BluetoothDevice> getDeviceMap(SharedPreferences preferences) {
        String deviceMapJson = preferences.getString("deviceMap", null);
        HashMap<Integer, BluetoothDevice> deviceMap = null;
        if (deviceMapJson != null) {
            Type stringStringMap = new TypeToken<Map<Integer, BluetoothDevice>>() {
            }.getType();
            deviceMap = gson.fromJson(deviceMapJson, stringStringMap);
        } else {
            deviceMap = new HashMap<>();
        }
        return deviceMap;
    }

    public static BluetoothDevice getDevice(Context context, int deviceNumber) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        HashMap<Integer, BluetoothDevice> deviceMap = getDeviceMap(preferences);

        return deviceMap.get(deviceNumber);
    }

}
