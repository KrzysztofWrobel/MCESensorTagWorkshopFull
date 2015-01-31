package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.zinno.sensortag.BleService;
import com.zinno.sensortag.BleServiceBindingActivity;
import com.zinno.sensortag.info.TiInfoService;
import com.zinno.sensortag.sensor.TiGyroscopeSensor;
import com.zinno.sensortag.sensor.TiKeysSensor;
import com.zinno.sensortag.sensor.TiSensor;
import com.zinno.sensortag.sensor.TiSensors;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TwitterActivity extends BleServiceBindingActivity {
    private static final String TAG = TwitterActivity.class.getSimpleName();

    TiSensor<?> keysSensor, gyroscopeSensor;

    boolean sensorEnabled = false;

    @InjectView(R.id.leftTextView)
    TextView leftTextView;

    @InjectView(R.id.rightTextView)
    TextView rightTextView;
    private TiInfoService infoService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        infoService = TiInfoServices.getService(TiKeysSensor.UUID_SERVICE);
        keysSensor = TiSensors.getSensor(TiKeysSensor.UUID_SERVICE);
        gyroscopeSensor = TiSensors.getSensor(TiGyroscopeSensor.UUID_SERVICE);

        Log.d(TAG, "keysSensor=" + keysSensor);
        Log.d(TAG, "gyroscopeSensor=" + gyroscopeSensor);

        setContentView(R.layout.activity_twitter);

        ButterKnife.inject(this);
    }

    @Override
    public void onDisconnected() {
        finish();
    }

    @Override
    public void onServiceDiscovered() {
        sensorEnabled = true;

//        getBleService().enableSensor(gyroscopeSensor, true);
//        Log.d(TAG, "XXX gyroscopeSensor=" + gyroscopeSensor);

        Log.d(TAG, "XXX keysSensor=" + keysSensor);
        getBleService().enableSensor(getDeviceName(), keysSensor, true);

//        sensor.notify(true);

//        Log.d(TAG, "value=" + sensor.getData());

//        if (sensor instanceof TiPeriodicalSensor) {
//            TiPeriodicalSensor periodicalSensor = (TiPeriodicalSensor) sensor;
//            periodicalSensor.setPeriod(periodicalSensor.getMinPeriod());
//            getBleService().getBleManager().updateSensor(sensor);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        BleService bleService = getBleService();
        if (bleService != null && sensorEnabled) {
            bleService.enableSensor(getDeviceName(), gyroscopeSensor, false);
            bleService.enableSensor(getDeviceName(), keysSensor, false);
        }

        super.onPause();
    }

    @Override
    public void onDataAvailable(String serviceUuid, String characteristicUUid, String text, byte[] data) {
        Log.d(TAG, String.format("ServiceUUID: %s, CharacteristicUUIS: %s", serviceUuid, characteristicUUid));
        Log.d(TAG, String.format("Data: %s", text));

//        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
//        final TiKeysSensor tiKeysSensor = (TiKeysSensor) tiSensor;
//        TiKeysSensor.SimpleKeysStatus simpleKeysStatus = tiKeysSensor.getData();

        /*
        String split[] = text.split("\n");
        if (split.length != 3) {
            Log.e(TAG, "onDataAvailable: text split != 3");
            return;
        }

        String zStr = split[2];

        split = zStr.split("=");
        if (split.length != 2) {
            Log.e(TAG, "onDataAvailable: z value split != 2");
            return;
        }

        float z = Float.valueOf(split[1]);
        Log.d(TAG, "onDataAvailable: z=" + z);

        long currentTime = System.currentTimeMillis();

        if (lastTime == -1) {
            lastTime = currentTime;
            firstZ = z;
            return;
        }

        // in millis
        float deltaTime = currentTime - lastTime;
        Log.d(TAG, "deltaTime=" + deltaTime);

        Log.d(TAG, "z-lastZ=" + (z - firstZ));

        float deltaAngle = (z - firstZ) * (deltaTime / 1000f);
        Log.d(TAG, "deltaAngle=" + deltaAngle);

        rotationZ += deltaAngle;

        Log.d(TAG, "rotation " + Math.abs(rotationZ) % 360);

        lastTime = currentTime;
        */
    }
}
