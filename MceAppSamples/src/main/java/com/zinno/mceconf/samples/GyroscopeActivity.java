package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.zinno.sensortag.BleService;
import com.zinno.sensortag.BleServiceBindingActivity;
import com.zinno.sensortag.sensor.TiGyroscopeSensor;
import com.zinno.sensortag.sensor.TiPeriodicalSensor;
import com.zinno.sensortag.sensor.TiSensor;
import com.zinno.sensortag.sensor.TiSensors;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GyroscopeActivity extends BleServiceBindingActivity {
    private static final String TAG = GyroscopeActivity.class.getSimpleName();

    @InjectView(R.id.action_bar)
    Toolbar toolbar;

    @InjectView(R.id.angleTextView)
    TextView angleTextView;

    @InjectView(R.id.rpmTextView)
    TextView rpmTextView;

    TiSensor<?> sensor;

    boolean sensorEnabled = false;

    long lastTime = -1;
    float firstZ = 0f;
    float rotationZ = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gyroscope);

        ButterKnife.inject(this);

        sensor = TiSensors.getSensor(TiGyroscopeSensor.UUID_SERVICE);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GyroscopeActivity.this.finish();
            }
        });
        toolbar.setTitle(R.string.gyroscope_sample_name);
    }

    @Override
    public void onDisconnected(String deviceAddress) {
        finish();
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
        sensorEnabled = true;
        getBleService().enableSensor(getDeviceAddress(), sensor, true);

        if (sensor instanceof TiPeriodicalSensor) {
            TiPeriodicalSensor periodicalSensor = (TiPeriodicalSensor) sensor;
            periodicalSensor.setPeriod(periodicalSensor.getMinPeriod());
            getBleService().getBleManager().updateSensor(deviceAddress, sensor);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        BleService bleService = getBleService();
        if (bleService != null && sensorEnabled) {
            bleService.enableSensor(getDeviceAddress(), sensor, false);
        }

        super.onPause();
    }

    public static class Dimens {
        float x, y, z;

        public Dimens(float values[]) {
            this.x = values[0];
            this.y = values[1];
            this.z = values[2];
        }

        @Override
        public String toString() {
            return "Dimens{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }

        public static Dimens fromString(String data) {
            String split[] = data.split("\n");
            if (split.length != 3) {
                Log.e(TAG, "Dimens::fromString text split != 3");
                return null;
            }

            float values[] = new float[3];

            for (int i = 0; i < split.length; i++) {
                String valSplit[] = split[i].split("=");
                if (valSplit.length != 2) {
                    Log.e(TAG, "Dimens::fromString val split != 2: " + split[i]);
                    return null;
                }

                values[i] = Float.valueOf(valSplit[1]);
            }

            return new Dimens(values);
        }
    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, byte[] data) {
        Dimens dimens = Dimens.fromString(text);
        if (dimens == null) {
            Log.e(TAG, "onDataAvailable: cannot create Dimens from '" + text + "'");
            return;
        }

        Log.d(TAG, "dimens=" + dimens);

        long currentTime = System.currentTimeMillis();

        // calibration
        if (lastTime == -1) {
            lastTime = currentTime;
            firstZ = dimens.z;
            return;
        }

        // in millis
        float deltaTime = currentTime - lastTime;
//        Log.d(TAG, "deltaTime=" + deltaTime);
//        Log.d(TAG, "z-lastZ=" + (z - firstZ));

        float deltaAngle = (dimens.z - firstZ) * (deltaTime / 1000f);
//        Log.d(TAG, "deltaAngle=" + deltaAngle);

        rotationZ += deltaAngle;

        long rotation = (long) (Math.abs(rotationZ) % 360);
        angleTextView.setText(String.valueOf(rotation));

        long rpm = (long) (rotation / 6f); // length / 360 deg * 60s (to get RPM)
        rpmTextView.setText(String.valueOf(rpm));

        lastTime = currentTime;
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
