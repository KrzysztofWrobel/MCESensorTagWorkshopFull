package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zinno.sensortag.BleService;
import com.zinno.sensortag.BleServiceBindingActivity;
import com.zinno.sensortag.sensor.TiKeysSensor;
import com.zinno.sensortag.sensor.TiSensor;
import com.zinno.sensortag.sensor.TiSensors;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by krzysztofwrobel on 31/01/15.
 */
public class RunningButtonGameActivity extends BleServiceBindingActivity implements SeekBar.OnSeekBarChangeListener {
    public static final String TAG = RunningButtonGameActivity.class.getSimpleName();

    private TiSensor<?> buttonSensor;

    @InjectView(R.id.tv_speed_player_one)
    TextView player1SpeedTextView;

    @InjectView(R.id.tv_speed_player_two)
    TextView player2SpeedTextView;

    @InjectView(R.id.tv_race_summary)
    TextView raceSummaryTextView;

    @InjectView(R.id.sb_player_one)
    SeekBar player1StatusSeekBar;

    @InjectView(R.id.sb_player_two)
    SeekBar player2StatusSeekBar;

    @InjectView(R.id.b_reset)
    Button resetButton;

    @InjectView(R.id.action_bar)
    Toolbar toolbar;

    private boolean sensorEnabled;
    private TiKeysSensor.SimpleKeysStatus player1KeyStatus;
    private TiKeysSensor.SimpleKeysStatus player2KeyStatus;
    private boolean gameStarted = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_buttons);

        buttonSensor = TiSensors.getSensor(TiKeysSensor.UUID_SERVICE);

        ButterKnife.inject(this);

        player1StatusSeekBar.setClickable(false);
        player1StatusSeekBar.setOnSeekBarChangeListener(this);
        player2StatusSeekBar.setClickable(false);
        player2StatusSeekBar.setOnSeekBarChangeListener(this);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RunningButtonGameActivity.this.finish();
            }
        });
        toolbar.setTitle(R.string.button_game_sample_name);
    }

    private void resetGame() {
        gameStarted = true;
        raceSummaryTextView.setText("Go go go!");
        player1StatusSeekBar.setProgress(0);
        player2StatusSeekBar.setProgress(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        BleService bleService = getBleService();
        if (bleService != null && sensorEnabled) {
            for (String address : getDeviceAddresses()) {
                getBleService().enableSensor(address, buttonSensor, true);
            }
        }
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
        sensorEnabled = true;
        Log.d(TAG, "onServiceDiscovered");

        getBleService().enableSensor(deviceAddress, buttonSensor, true);

    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, byte[] data) {
        Log.d(TAG, String.format("DeviceAddress: %s,ServiceUUID: %s, CharacteristicUUIS: %s", deviceAddress, serviceUuid, characteristicUUid));
        Log.d(TAG, String.format("Data: %s", text));

        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
        final TiKeysSensor tiKeysSensor = (TiKeysSensor) tiSensor;
        TiKeysSensor.SimpleKeysStatus simpleKeysStatus = tiKeysSensor.getData();

        if (gameStarted && (simpleKeysStatus == TiKeysSensor.SimpleKeysStatus.ON_OFF || simpleKeysStatus == TiKeysSensor.SimpleKeysStatus.OFF_ON)) {

            int playerNumber = getDeviceAddresses().indexOf(deviceAddress);
            switch (playerNumber) {
                case 0:
                    if (player1KeyStatus != simpleKeysStatus) {
                        player1StatusSeekBar.incrementProgressBy(1);
                    }
                    player1KeyStatus = simpleKeysStatus;
                    break;
                case 1:
                    if (player2KeyStatus != simpleKeysStatus) {
                        player2StatusSeekBar.incrementProgressBy(1);
                    }
                    player2KeyStatus = simpleKeysStatus;
                    break;
            }
        }

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getProgress() == seekBar.getMax()) {
            gameStarted = false;
            if (seekBar.equals(player1StatusSeekBar)) {
                raceSummaryTextView.setText("Player 1 WON!");
            } else if (seekBar.equals(player2StatusSeekBar)) {
                raceSummaryTextView.setText("Player 2 WON!");
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
