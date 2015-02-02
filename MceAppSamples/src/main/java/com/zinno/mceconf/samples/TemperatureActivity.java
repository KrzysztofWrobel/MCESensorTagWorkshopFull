package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zinno.sensortag.BleService;
import com.zinno.sensortag.BleServiceBindingActivity;
import com.zinno.sensortag.sensor.TiHumiditySensor;
import com.zinno.sensortag.sensor.TiSensor;
import com.zinno.sensortag.sensor.TiSensors;
import com.zinno.sensortag.sensor.TiTemperatureSensor;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class TemperatureActivity extends BleServiceBindingActivity {
    private static final String TAG = TemperatureActivity.class.getSimpleName();

    private static final double THRESHOLD = 15;
    private static final int valuesRange = 20;

    private float values[] = new float[valuesRange];
    private int valuesIdx = 0;
    private long valuesCount = 0;

    @InjectView(R.id.tempTextView)
    TextView tempTextView;

    @InjectView(R.id.hugTextView)
    TextView hugTextView;

    private TiSensor<?> tempSensor;

    private boolean sensorEnabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hygrometer);

        ButterKnife.inject(this);

        tempSensor = TiSensors.getSensor(TiTemperatureSensor.UUID_SERVICE);
    }

    private void estimateValues() {
        float min = Float.MAX_VALUE;
        float max = 0.0f;

        for (int idx = 0; idx < valuesRange; ++idx) {
            min = (min > values[idx] ? values[idx] : min);
            max = (max < values[idx] ? values[idx] : max);
        }

        Log.d(TAG, "min=" + min + ", max=" + max + ", diff=" + (max - min));

        if (max - min > THRESHOLD &&
                (valuesIdx % valuesRange != 0)) {
            hugTextView.setVisibility(View.VISIBLE);
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
        Log.d(TAG, String.format("DeviceAddress: %s,ServiceUUID: %s, CharacteristicUUIS: %s", deviceAddress, serviceUuid, characteristicUUid));
        Log.d(TAG, String.format("Data: %s", text));

        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
        final TiTemperatureSensor temperatureSensor = (TiTemperatureSensor) tiSensor;

        float temp[] = temperatureSensor.getData();
        if (temp.length != 2) {
            return;
        }

        values[valuesIdx] = temp[1];

        tempTextView.setText("Temperature: " + values[valuesIdx]);
        valuesIdx = (valuesIdx + 1) % valuesRange;

        if (valuesCount > valuesRange)
            estimateValues();

        valuesCount++;
    }
}
