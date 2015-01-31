package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zinno.sensortag.BleServiceBindingActivity;
import com.zinno.sensortag.sensor.TiKeysSensor;
import com.zinno.sensortag.sensor.TiSensor;
import com.zinno.sensortag.sensor.TiSensors;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by krzysztofwrobel on 31/01/15.
 */
public class RunningButtonGameActivity extends BleServiceBindingActivity {
    public static final String TAG = RunningButtonGameActivity.class.getSimpleName();

    private TiSensor<?> buttonSensor;

    @InjectView(R.id.tv_speed_player_one)
    public TextView player1SpeedTextView;

    @InjectView(R.id.tv_speed_player_two)
    public TextView player2SpeedTextView;

    @InjectView(R.id.tv_race_summary)
    public TextView raceSummaryTextView;

    @InjectView(R.id.pb_player_one)
    public ProgressBar player1StatusProgressBar;

    @InjectView(R.id.pb_player_two)
    public ProgressBar player2StatusProgressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_buttons);

        buttonSensor = TiSensors.getSensor(TiKeysSensor.UUID_SERVICE);

        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onServiceDiscovered() {
        Log.d(TAG, "onServiceDiscovered");

        for (String address : getDeviceAddresses()) {
            getBleService().enableSensor(address, buttonSensor, true);
        }

    }

    @Override
    public void onDataAvailable(String serviceUuid, String characteristicUUid, String text, byte[] data) {
        Log.d(TAG, String.format("ServiceUUID: %s, CharacteristicUUIS: %s", serviceUuid, characteristicUUid));
        Log.d(TAG, String.format("Data: %s", text));

        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
        final TiKeysSensor tiKeysSensor = (TiKeysSensor) tiSensor;
        TiKeysSensor.SimpleKeysStatus simpleKeysStatus = tiKeysSensor.getData();


    }


    @Override
    public void onConnected() {
        super.onConnected();
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
    }

}
