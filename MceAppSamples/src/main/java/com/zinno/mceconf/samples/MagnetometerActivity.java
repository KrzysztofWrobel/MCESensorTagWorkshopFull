package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.zinno.sensortag.BleService;
import com.zinno.sensortag.BleServiceBindingActivity;
import com.zinno.sensortag.sensor.TiMagnetometerSensor;
import com.zinno.sensortag.sensor.TiPeriodicalSensor;
import com.zinno.sensortag.sensor.TiSensor;
import com.zinno.sensortag.sensor.TiSensors;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MagnetometerActivity extends BleServiceBindingActivity {
    private static final String TAG = MagnetometerActivity.class.getSimpleName();
    private TiSensor<?> magnetometerSensor;
    private boolean sensorEnabled;

    @InjectView(R.id.sw_high_low)

    Switch switchHighLow;
    @InjectView(R.id.b_calibrate)
    Button calibrateButton;

    @InjectView(R.id.action_bar)
    Toolbar toolbar;

    private static final float OFFSET = 20;
    private double lastValue;

    private enum State {
        LOW,
        HIGH
    }

    private State state = State.LOW;

    private ArrayList<float[]> calibrateValues;
    private float[] environmentCalibrateValue = new float[]{0, 0, 0};
    boolean calibrateEnv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnetometer);
        ButterKnife.inject(this);

        magnetometerSensor = TiSensors.getSensor(TiMagnetometerSensor.UUID_SERVICE);
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
                MagnetometerActivity.this.finish();
            }
        });
        toolbar.setTitle(R.string.magnetometer_sample_name);
    }


    @Override
    protected void onPause() {
        super.onPause();

        BleService bleService = getBleService();
        if (bleService != null && sensorEnabled) {
            for (String address : getDeviceAddresses()) {
                getBleService().enableSensor(address, magnetometerSensor, true);
            }
        }
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
        sensorEnabled = true;
        Log.d(TAG, "onServiceDiscovered");

        getBleService().enableSensor(deviceAddress, magnetometerSensor, true);
        if (magnetometerSensor instanceof TiPeriodicalSensor) {
            TiPeriodicalSensor periodicalSensor = (TiPeriodicalSensor) magnetometerSensor;
            periodicalSensor.setPeriod(periodicalSensor.getMinPeriod());

            getBleService().getBleManager().updateSensor(deviceAddress, magnetometerSensor);
        }

    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, byte[] data) {
        Log.d(TAG, String.format("DeviceAddress: %s,ServiceUUID: %s, CharacteristicUUIS: %s", deviceAddress, serviceUuid, characteristicUUid));
        Log.d(TAG, String.format("Data: %s", text));


        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
        final TiMagnetometerSensor tiMagnetometerSensor = (TiMagnetometerSensor) tiSensor;
        float[] magneticValues = tiMagnetometerSensor.getData();

        if (calibrateEnv) {
            calibrateValues.add(magneticValues);
        } else {
            float value = (magneticValues[2] - environmentCalibrateValue[2]);

            switch (state) {
                case LOW:
                    if (lastValue - value > OFFSET) {
                        state = State.HIGH;
                        lastValue = value;
                    }
                    break;
                case HIGH:
                    if (value - lastValue > OFFSET) {
                        state = State.LOW;
                        lastValue = value;
                    }
                    break;
            }

            switchHighLow.setChecked(state == State.HIGH);

        }
        if (calibrateEnv && calibrateValues.size() > 10) {

            float[] calibrateValue = new float[]{0, 0, 0};
            for (float[] values : calibrateValues) {
                calibrateValue[0] += values[0] / calibrateValues.size();
                calibrateValue[1] += values[1] / calibrateValues.size();
                calibrateValue[2] += values[2] / calibrateValues.size();
            }

            environmentCalibrateValue = calibrateValue;
            lastValue = calibrateValue[2];
            calibrateEnv = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_magnetometer, menu);
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
