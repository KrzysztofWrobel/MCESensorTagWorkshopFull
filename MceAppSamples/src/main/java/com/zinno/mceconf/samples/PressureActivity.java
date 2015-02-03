package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import com.zinno.sensortag.BleService;
import com.zinno.sensortag.BleServiceBindingActivity;
import com.zinno.sensortag.sensor.TiPressureSensor;
import com.zinno.sensortag.sensor.TiSensor;
import com.zinno.sensortag.sensor.TiSensors;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class PressureActivity extends BleServiceBindingActivity {
    private static final String TAG = PressureActivity.class.getSimpleName();
    private static final double OFFSET = 2000;//in Pascals

    @InjectView(R.id.b_calibrate)
    Button calibrateButton;

    @InjectView(R.id.tb_free_occupied)
    ToggleButton freeOccupiedToggleButton;

    @InjectView(R.id.action_bar)
    Toolbar toolbar;

    private enum State {
        FREE,
        OCCUPIED
    }

    private State state = State.FREE;

    private double lastValue;
    private TiSensor<?> pressureSensor;
    private ArrayList<Double> calibrateValues;
    private double environmentCalibrateValue;
    boolean calibrateEnv;
    private boolean sensorEnabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barometer);

        ButterKnife.inject(this);
        pressureSensor = TiSensors.getSensor(TiPressureSensor.UUID_SERVICE);
        calibrateValues = new ArrayList<>();

        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibrateValues = new ArrayList<>();
                calibrateEnv = true;
            }
        });

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PressureActivity.this.finish();
            }
        });
        toolbar.setTitle(R.string.barometer_sample_name);
    }

    @Override
    protected void onPause() {
        super.onPause();

        BleService bleService = getBleService();
        if (bleService != null && sensorEnabled) {
            for (String address : getDeviceAddresses()) {
                getBleService().enableSensor(address, pressureSensor, true);
            }
        }
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
        sensorEnabled = true;
        Log.d(TAG, "onServiceDiscovered");

        getBleService().enableSensor(deviceAddress, pressureSensor, true);
    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, byte[] data) {
        Log.d(TAG, String.format("DeviceAddress: %s,ServiceUUID: %s, CharacteristicUUIS: %s", deviceAddress, serviceUuid, characteristicUUid));
        Log.d(TAG, String.format("Data: %s", text));


        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
        final TiPressureSensor tiPressureSensor = (TiPressureSensor) tiSensor;
        Double pressureValue = tiPressureSensor.getData();

        if (calibrateEnv) {
            calibrateValues.add(pressureValue);
        } else {
            double value = pressureValue - environmentCalibrateValue;

            switch (state) {
                case FREE:
                    if (value - lastValue > OFFSET) {
                        state = State.OCCUPIED;
                        lastValue = value;
                    }
                    break;
                case OCCUPIED:
                    if (lastValue - value > OFFSET) {
                        state = State.FREE;
                        lastValue = value;
                    }
                    break;
            }

            freeOccupiedToggleButton.setChecked(state == State.OCCUPIED);

        }
        if (calibrateEnv && calibrateValues.size() > 3) {

            double calibrateValue = 0;
            for (double value : calibrateValues) {
                calibrateValue += value / calibrateValues.size();
            }

            environmentCalibrateValue = calibrateValue;
            lastValue = calibrateValue;
            calibrateEnv = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_barometer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
