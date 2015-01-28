package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.zinno.sensortag.BleServiceBindingActivity;
import com.zinno.sensortag.MathUtils;
import com.zinno.sensortag.sensor.TiAccelerometerSensor;
import com.zinno.sensortag.sensor.TiPeriodicalSensor;
import com.zinno.sensortag.sensor.TiSensor;
import com.zinno.sensortag.sensor.TiSensors;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class DiceActivity extends BleServiceBindingActivity {
    public static final String TAG = DiceActivity.class.getSimpleName();

    private TiSensor<?> accelerationSensor;
    private float[] lastAccelerationValues;
    private long startTimeout;

    @InjectView(R.id.tv_dice_state)
    public TextView diceStateTextView;

    @InjectView(R.id.tv_rolled_number)
    public TextView rolledNumberTextView;

    private enum DiceState {
        ROLLING,
        MAY_ROLL,
        MAY_STOP,
        STOPPED
    }

    private DiceState diceState = DiceState.STOPPED;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice);

        accelerationSensor = TiSensors.getSensor(TiAccelerometerSensor.UUID_SERVICE);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dice, menu);
        return true;
    }

    @Override
    public void onServiceDiscovered() {
        Log.d(TAG, "onServiceDiscovered");

        getBleService().enableSensor(accelerationSensor, true);
        if (accelerationSensor instanceof TiPeriodicalSensor) {
            TiPeriodicalSensor periodicalSensor = (TiPeriodicalSensor) accelerationSensor;
            periodicalSensor.setPeriod(periodicalSensor.getMinPeriod());

            getBleService().getBleManager().updateSensor(accelerationSensor);
        }
    }

    @Override
    public void onDataAvailable(String serviceUuid, String characteristicUUid, String text, byte[] data) {
        Log.d(TAG, String.format("ServiceUUID: %s, CharacteristicUUIS: %s", serviceUuid, characteristicUUid));
        Log.d(TAG, String.format("Data: %s", text));

        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
        final TiAccelerometerSensor tiAccelerometerSensor = (TiAccelerometerSensor) tiSensor;
        float[] accelerationValues = tiAccelerometerSensor.getData();

        if (lastAccelerationValues != null) {
            switch (diceState) {
                case ROLLING:
                    if (MathUtils.cosineSimilarity(lastAccelerationValues, accelerationValues) > 0.9) {
                        startTimeout = System.currentTimeMillis();
                        changeDiceState(DiceState.MAY_STOP);
                    }
                    break;
                case MAY_STOP:
                    if (MathUtils.cosineSimilarity(lastAccelerationValues, accelerationValues) > 0.9) {
                        if (System.currentTimeMillis() - startTimeout > 200) {
                            changeDiceState(DiceState.STOPPED);
                        }
                    } else {
                        changeDiceState(DiceState.ROLLING);
                    }
                    break;
                case STOPPED:
                    //Check if acceleration vector
                    if (MathUtils.cosineSimilarity(lastAccelerationValues, accelerationValues) < 0.9) {
                        startTimeout = System.currentTimeMillis();
                        changeDiceState(DiceState.MAY_ROLL);
                    }
                    break;
                case MAY_ROLL:
                    if (MathUtils.cosineSimilarity(lastAccelerationValues, accelerationValues) < 0.9) {
                        if (System.currentTimeMillis() - startTimeout > 50) {
                            changeDiceState(DiceState.ROLLING);
                        }
                    } else {
                        changeDiceState(DiceState.STOPPED);
                    }
                    break;
            }

        }

        lastAccelerationValues = accelerationValues;
    }

    private void changeDiceState(DiceState newState) {
        diceState = newState;
        diceStateTextView.setText(diceState.toString());

        if (newState == DiceState.STOPPED) {
            int rolledNumber = getRolledNumber();
            rolledNumberTextView.setText(String.format("You have rolled: %d !", rolledNumber));
        }
    }

    private int getRolledNumber() {
        float x = Math.abs(lastAccelerationValues[0]);
        float y = Math.abs(lastAccelerationValues[1]);
        float z = Math.abs(lastAccelerationValues[2]);

        int rolled = 0;
        if (x < y) {
            if (y < z) {
                //z is the biggest
                rolled = lastAccelerationValues[2] < 0 ? 1 : 6;
            } else {
                //y is the biggest
                rolled = lastAccelerationValues[1] < 0 ? 2 : 5;
            }
        } else {
            if (x < z) {
                //z is the biggest
                rolled = lastAccelerationValues[2] < 0 ? 1 : 6;
            } else {
                //x is the biggest
                rolled = lastAccelerationValues[0] < 0 ? 3 : 4;
            }
        }

        return rolled;
    }


    @Override
    public void onConnected() {
        super.onConnected();
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
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
