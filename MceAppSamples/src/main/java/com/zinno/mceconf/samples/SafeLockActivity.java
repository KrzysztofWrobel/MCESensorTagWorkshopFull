package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joanzapata.android.iconify.IconTextView;
import com.joanzapata.android.iconify.Iconify;
import com.zinno.sensortag.BleService;
import com.zinno.sensortag.BleServiceBindingActivity;
import com.zinno.sensortag.sensor.TiGyroscopeSensor;
import com.zinno.sensortag.sensor.TiPeriodicalSensor;
import com.zinno.sensortag.sensor.TiSensor;
import com.zinno.sensortag.sensor.TiSensors;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class SafeLockActivity extends BleServiceBindingActivity {
    private static final String TAG = SafeLockActivity.class.getSimpleName();
    private static final float THRESHOLD = 25;
    private static final float MAX_RANGE = 20;

    private static String lockFontAwesome = "{fa-lock}";
    private static String unlockFontAwesome = "{fa-unlock}";

    @InjectView(R.id.tv_current_sequence)
    TextView currentSequenceTextView;

    @InjectView(R.id.ictv_lock)
    IconTextView lockIconTextView;

    @InjectView(R.id.ll_sequence)
    LinearLayout openSafeSequenceStatus;

    @InjectView(R.id.b_reset)
    Button resetButton;

    TiSensor<?> gyroscopeSensor;
    boolean sensorEnabled = false;
    private ArrayList<SafeSequence> openSafeSequences;
    private ArrayList<SafeSequence> currentSafeSequences;
    private SafeSequence.Direction previousDirection = null;

    private static class SafeSequence {
        public enum Direction {
            LEFT,
            RIGHT
        }

        private Direction direction;
        private int range;

        private SafeSequence(Direction direction, int range) {
            this.direction = direction;
            this.range = range;
        }

        public Direction getDirection() {
            return direction;
        }

        public int getRange() {
            return range;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SafeSequence that = (SafeSequence) o;

            if (range != that.range) return false;
            if (direction != that.direction) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = direction != null ? direction.hashCode() : 0;
            result = 31 * result + range;
            return result;
        }
    }

    long lastTime = -1;
    float referenceZ = 0f;
    float rotationZ = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_lock);
        ButterKnife.inject(this);

        gyroscopeSensor = TiSensors.getSensor(TiGyroscopeSensor.UUID_SERVICE);
        openSafeSequences = new ArrayList<>();
        openSafeSequences.add(new SafeSequence(SafeSequence.Direction.LEFT, 3));
        openSafeSequences.add(new SafeSequence(SafeSequence.Direction.RIGHT, 4));
        openSafeSequences.add(new SafeSequence(SafeSequence.Direction.LEFT, 5));
        openSafeSequences.add(new SafeSequence(SafeSequence.Direction.RIGHT, 6));

        currentSafeSequences = new ArrayList<>();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        for (SafeSequence safeSequence : openSafeSequences) {
            View safeView = new View(this);
            safeView.setLayoutParams(params);
            openSafeSequenceStatus.addView(safeView);
        }
        resetSequence();

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetSequence();
            }
        });
    }

    private void resetSequence() {
        lastTime = -1;
        currentSafeSequences.clear();
        currentSequenceTextView.setText("");
        for (int i = 0; i < openSafeSequenceStatus.getChildCount(); i++) {
            View view = openSafeSequenceStatus.getChildAt(i);
            view.setBackgroundColor(getResources().getColor(R.color.red));
        }

        setIconLocked(true);
    }

    private void setIconLocked(boolean locked) {
        String iconString;
        if (locked) {
            iconString = lockFontAwesome;
        } else {
            iconString = unlockFontAwesome;
        }
        lockIconTextView.setText(iconString);
        Iconify.addIcons(lockIconTextView);
    }

    private void validateSequence(SafeSequence safeSequence) {
        int nextSequenceIdx = currentSafeSequences.size();
        if (nextSequenceIdx < openSafeSequences.size() && openSafeSequences.get(nextSequenceIdx).equals(safeSequence)) {
            View view = openSafeSequenceStatus.getChildAt(nextSequenceIdx);
            int color = getResources().getColor(R.color.green);
            view.setBackgroundColor(color);
            currentSafeSequences.add(safeSequence);
            if (currentSafeSequences.size() == openSafeSequences.size()) {
                setIconLocked(false);
            }
        }
    }


    @Override
    public void onDisconnected(String deviceAddress) {
        finish();
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
        sensorEnabled = true;
        getBleService().enableSensor(getDeviceAddress(), gyroscopeSensor, true);

        if (gyroscopeSensor instanceof TiPeriodicalSensor) {
            TiPeriodicalSensor periodicalSensor = (TiPeriodicalSensor) gyroscopeSensor;
            periodicalSensor.setPeriod(periodicalSensor.getMinPeriod());
            getBleService().getBleManager().updateSensor(deviceAddress, gyroscopeSensor);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        BleService bleService = getBleService();
        if (bleService != null && sensorEnabled) {
            bleService.enableSensor(getDeviceAddress(), gyroscopeSensor, false);
        }

        super.onPause();
    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, byte[] data) {
        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
        final TiGyroscopeSensor tiGyroscopeSensor = (TiGyroscopeSensor) tiSensor;
        float[] tiGyroscopeSensorData = tiGyroscopeSensor.getData();

        //We get the angular velocity around z-axis
        float z = tiGyroscopeSensorData[2];
        long currentTime = System.currentTimeMillis();

        if (lastTime == -1) {
            //We need some reference time first
            lastTime = currentTime;
            referenceZ = z;
            return;
        }

        float deltaTime = currentTime - lastTime;
        if (Math.abs(z) > THRESHOLD) {
            if (z > 0) {
                //Rotating left
                if (previousDirection == SafeSequence.Direction.RIGHT) {
                    int currentValue = (int) Math.abs(rotationZ / 360 * MAX_RANGE);
                    SafeSequence safeSequence = new SafeSequence(SafeSequence.Direction.RIGHT, currentValue);
                    validateSequence(safeSequence);

                    calibrateRotationReference();
                }
                previousDirection = SafeSequence.Direction.LEFT;
            } else {
                //Rotating right
                if (previousDirection == SafeSequence.Direction.LEFT) {
                    int currentValue = (int) Math.abs(rotationZ / 360 * MAX_RANGE);
                    SafeSequence safeSequence = new SafeSequence(SafeSequence.Direction.LEFT, currentValue);
                    validateSequence(safeSequence);

                    calibrateRotationReference();
                }
                previousDirection = SafeSequence.Direction.RIGHT;
            }


            float deltaAngle = (z - referenceZ) * (deltaTime / 1000f);
            rotationZ += deltaAngle;
            int currentValue = (int) Math.abs(rotationZ / 360 * MAX_RANGE);
            currentSequenceTextView.setText(String.format("%s - %d", ((previousDirection == SafeSequence.Direction.LEFT) ? "L" : "R"), currentValue));
            Log.d(TAG, "deltaAngle=" + deltaAngle);
        }

        lastTime = currentTime;
    }

    private void calibrateRotationReference() {
        referenceZ = rotationZ;
        rotationZ = 0;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_safe_lock, menu);
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
