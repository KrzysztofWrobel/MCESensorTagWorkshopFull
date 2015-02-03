package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, byte[] data) {
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
