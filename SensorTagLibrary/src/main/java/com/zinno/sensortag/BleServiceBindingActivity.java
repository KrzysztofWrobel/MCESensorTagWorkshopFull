package com.zinno.sensortag;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.zinno.sensortag.ble.BleActionsReceiver;
import com.zinno.sensortag.ble.BleServiceListener;
import com.zinno.sensortag.config.AppConfig;

import java.util.ArrayList;

public abstract class BleServiceBindingActivity extends ActionBarActivity
        implements BleServiceListener,
        ServiceConnection {
    private final static String TAG = BleServiceBindingActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_NAMES = "DEVICE_NAMES";
    public static final String EXTRAS_DEVICE_ADDRESSES = "DEVICE_ADDRESSES";

    private String deviceName;
    private String deviceAddress;
    private BleService bleService;
    @SuppressWarnings("ConstantConditions")
    private BroadcastReceiver bleActionsReceiver =
            AppConfig.REMOTE_BLE_SERVICE ? new BleActionsReceiver(this) : null;
    private ArrayList<String> deviceNames;
    private ArrayList<String> deviceAddresses;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        deviceNames = intent.getStringArrayListExtra(EXTRAS_DEVICE_NAMES);
        deviceAddresses = intent.getStringArrayListExtra(EXTRAS_DEVICE_ADDRESSES);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (AppConfig.REMOTE_BLE_SERVICE) {
            registerReceiver(bleActionsReceiver, BleActionsReceiver.createIntentFilter());
        }
        final Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bleService != null) {
            bleService.getBleManager().disconnect();
        }
        if (AppConfig.REMOTE_BLE_SERVICE) {
            unregisterReceiver(bleActionsReceiver);
        }
        unbindService(this);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public ArrayList<String> getDeviceNames() {
        return deviceNames;
    }

    public ArrayList<String> getDeviceAddresses() {
        return deviceAddresses;
    }

    public BleService getBleService() {
        return bleService;
    }

    @Override
    public void onConnected(String deviceAddress) {
    }

    @Override
    public void onDisconnected(String deviceAddress) {
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, byte[] data) {
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        bleService = ((BleService.LocalBinder) service).getService();
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (!AppConfig.REMOTE_BLE_SERVICE) {
            bleService.setServiceListener(this);
        }
        if (!bleService.getBleManager().initialize(getBaseContext())) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
            return;
        }

        // Automatically connects to the device upon successful start-up initialization.
        if (deviceAddresses != null) {
            for (String address : deviceAddresses) {
                bleService.getBleManager().connect(getBaseContext(), address);
            }
        } else {
            bleService.getBleManager().connect(getBaseContext(), deviceAddress);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        bleService = null;
        //TODO: show toast
    }
}
