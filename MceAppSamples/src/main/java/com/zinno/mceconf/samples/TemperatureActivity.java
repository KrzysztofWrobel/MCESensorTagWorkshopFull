package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.zinno.sensortag.BleService;
import com.zinno.sensortag.BleServiceBindingActivity;
import com.zinno.sensortag.sensor.TiSensor;
import com.zinno.sensortag.sensor.TiSensors;
import com.zinno.sensortag.sensor.TiTemperatureSensor;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class TemperatureActivity extends BleServiceBindingActivity {
    private static final String TAG = TemperatureActivity.class.getSimpleName();

    private static final double THRESHOLD = 5;
    private static final int valuesRange = 6;

    private float ambientTemp = 0;

    private float values[] = new float[valuesRange];
    private int valuesIdx = 0;
    private long valuesCount = 0;

    @InjectView(R.id.tempTextView)
    TextView tempTextView;

    @InjectView(R.id.hugTextView)
    TextView hugTextView;

    @InjectView(R.id.action_bar)
    Toolbar toolbar;

    private TiSensor<?> tempSensor;

    private boolean sensorEnabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_temperature);

        ButterKnife.inject(this);

        tempSensor = TiSensors.getSensor(TiTemperatureSensor.UUID_SERVICE);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TemperatureActivity.this.finish();
            }
        });
        toolbar.setTitle(R.string.temperature_sample_name);
    }

    private void estimateValues() {
        float avg = 0;

        for (int idx = 0; idx < valuesRange; ++idx) {
            avg += values[idx];
        }

        avg = avg / valuesRange;

//        Log.d(TAG, "avg=" + avg + ", ambient=" + ambientTemp + ", valuesIdx=" + valuesIdx);

        if (avg - ambientTemp >= THRESHOLD) {
            hugTextView.setVisibility(View.VISIBLE);
        } else {
            hugTextView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        BleService bleService = getBleService();
        if (bleService != null && sensorEnabled) {
            bleService.enableSensor(getDeviceAddress(), tempSensor, false);
        }

        super.onPause();
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
        sensorEnabled = true;

        getBleService().enableSensor(getDeviceAddress(), tempSensor, true);
    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, byte[] data) {
//        Log.d(TAG, String.format("DeviceAddress: %s,ServiceUUID: %s, CharacteristicUUIS: %s", deviceAddress, serviceUuid, characteristicUUid));
//        Log.d(TAG, String.format("Data: %s", text));

        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
        final TiTemperatureSensor temperatureSensor = (TiTemperatureSensor) tiSensor;

        float temp[] = temperatureSensor.getData();
        if (temp.length != 2) {
            return;
        }

        ambientTemp = temp[0];

        values[valuesIdx] = temp[1];

        tempTextView.setText("Temperature: " + values[valuesIdx]);
        valuesIdx = (valuesIdx + 1) % valuesRange;

        if (valuesCount > valuesRange)
            estimateValues();

        valuesCount++;
    }
}
