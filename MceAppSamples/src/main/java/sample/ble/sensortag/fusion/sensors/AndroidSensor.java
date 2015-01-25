package sample.ble.sensortag.fusion.sensors;

import android.hardware.Sensor;

public class AndroidSensor implements ISensor {

    public final Sensor sensor;

    public AndroidSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    @Override
    public float getMaxRange() {
        return sensor.getMaximumRange();
    }

    @Override
    public int getType() {
        return sensor.getType();
    }
}
