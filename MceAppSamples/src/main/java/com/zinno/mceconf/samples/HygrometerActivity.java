package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.zinno.sensortag.BleService;
import com.zinno.sensortag.BleServiceBindingActivity;
import com.zinno.sensortag.sensor.TiHumiditySensor;
import com.zinno.sensortag.sensor.TiSensor;
import com.zinno.sensortag.sensor.TiSensors;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class HygrometerActivity extends BleServiceBindingActivity {
    private static final String TAG = HygrometerActivity.class.getSimpleName();

    private static final double THRESHOLD = 20;

    private static final int valuesRange = 6;
    private static final int valueEdgesRange = 2;

    private float values[] = new float[valuesRange];
    private int valuesIdx = 0;
    private long valuesCount = 0;

    @InjectView(R.id.humidityTextView)
    TextView humidityTextView;

    @InjectView(R.id.stopTextView)
    TextView stopTextView;

    private TiSensor<?> humiditySensor;

    private boolean sensorEnabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hygrometer);

        ButterKnife.inject(this);

        humiditySensor = TiSensors.getSensor(TiHumiditySensor.UUID_SERVICE);
    }

    private boolean hasRaised() {
        float leftEdgeAvg = -1.0f;
        float rightEdgeAvg = -1.0f;

        for (int beginIdx = 0, endIdx = valuesRange - 1;
             beginIdx < valueEdgesRange && endIdx > valuesRange - valueEdgesRange - 1;
             ++beginIdx, --endIdx) {
            leftEdgeAvg += values[beginIdx];
            rightEdgeAvg += values[endIdx];
        }

        return (leftEdgeAvg / valueEdgesRange < rightEdgeAvg / valueEdgesRange);
    }

    private void estimateValues() {
        float min = Float.MAX_VALUE;
        float max = 0.0f;
//        float avg = 0.0f;

        for (int idx = 0; idx < valuesRange; ++idx) {
//            avg += values[idx];
            min = (min > values[idx] ? values[idx] : min);
            max = (max < values[idx] ? values[idx] : max);
        }

        Log.d(TAG, "min=" + min + ", max=" + max + ", diff=" + (max - min));
        Log.d(TAG, "hasRaised=" + hasRaised());

        if (!hasRaised() &&
                max - min > THRESHOLD &&
                (valuesIdx % valuesRange != 0)) {
            stopTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        BleService bleService = getBleService();
        if (bleService != null && sensorEnabled) {
            bleService.enableSensor(getDeviceAddress(), humiditySensor, false);
        }

        super.onPause();
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
        sensorEnabled = true;

        getBleService().enableSensor(getDeviceAddress(), humiditySensor, true);
    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, byte[] data) {
        Log.d(TAG, String.format("DeviceAddress: %s,ServiceUUID: %s, CharacteristicUUIS: %s", deviceAddress, serviceUuid, characteristicUUid));
        Log.d(TAG, String.format("Data: %s", text));

        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
        final TiHumiditySensor tiHumiditySensor = (TiHumiditySensor) tiSensor;

        values[valuesIdx] = tiHumiditySensor.getData();
        humidityTextView.setText("Humidity: " + values[valuesIdx]);
        valuesIdx = (valuesIdx + 1) % valuesRange;

        if (valuesCount > valuesRange)
            estimateValues();

        valuesCount++;
    }
}
