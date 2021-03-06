package com.zinno.sensortag.sensor;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.zinno.sensortag.ble.BleGattExecutor;
import com.zinno.sensortag.ble.BleGattExecutor.ServiceAction.ActionType;

import java.util.UUID;

/**
 * Created by steven on 9/3/13.
 */
public abstract class TiSensor<T> {
    @SuppressWarnings("UnusedDeclaration")
    private final static String TAG = TiSensor.class.getSimpleName();

    private static String CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    private T data;

    protected TiSensor() {
    }

    public abstract String getName();

    public String getCharacteristicName(String uuid) {
        if (uuid.equals(getDataUUID()))
            return getName() + " Data";
        else if (uuid.equals(getConfigUUID()))
            return getName() + " Config";
        return "Unknown";
    }

    public abstract String getServiceUUID();

    public abstract String getDataUUID();

    public abstract String getConfigUUID();

    public boolean isConfigUUID(String uuid) {
        return false;
    }

    public T getData() {
        return data;
    }

    public abstract String getDataString();

    public void onCharacteristicChanged(BluetoothGattCharacteristic c) {
        data = parse(c);
    }

    public boolean onCharacteristicRead(BluetoothGattCharacteristic c) {
        return false;
    }

    protected byte[] getConfigValues(boolean enable) {
        return new byte[]{(byte) (enable ? 1 : 0)};
    }

    protected abstract T parse(BluetoothGattCharacteristic c);

    public BleGattExecutor.ServiceAction[] enable(BluetoothGatt gatt, final boolean enable) {
        return new BleGattExecutor.ServiceAction[]{
                write(gatt, getConfigUUID(), getConfigValues(enable)),
                notify(gatt, enable)
        };
    }

    public BleGattExecutor.ServiceAction update(BluetoothGatt gatt) {
        return BleGattExecutor.ServiceAction.NULL;
    }

    public BleGattExecutor.ServiceAction read(BluetoothGatt gatt, final String uuid) {
        return new BleGattExecutor.ServiceAction(gatt, ActionType.READ) {
            @Override
            public boolean execute(BluetoothGatt bluetoothGatt) {
                final BluetoothGattCharacteristic characteristic = getCharacteristic(bluetoothGatt, uuid);
                bluetoothGatt.readCharacteristic(characteristic);
                return false;
            }
        };
    }

    public BleGattExecutor.ServiceAction write(BluetoothGatt gatt, final String uuid, final byte[] value) {
        return new BleGattExecutor.ServiceAction(gatt, ActionType.WRITE) {
            @Override
            public boolean execute(BluetoothGatt bluetoothGatt) {
                final BluetoothGattCharacteristic characteristic = getCharacteristic(bluetoothGatt, uuid);
                characteristic.setValue(value);
                bluetoothGatt.writeCharacteristic(characteristic);
                return false;
            }
        };
    }

    public BleGattExecutor.ServiceAction notify(BluetoothGatt gatt, final boolean start) {
        return new BleGattExecutor.ServiceAction(gatt, ActionType.NOTIFY) {
            @Override
            public boolean execute(BluetoothGatt bluetoothGatt) {
                final UUID CCC = UUID.fromString(CHARACTERISTIC_CONFIG);

                final BluetoothGattCharacteristic dataCharacteristic = getCharacteristic(bluetoothGatt, getDataUUID());
                final BluetoothGattDescriptor config = dataCharacteristic.getDescriptor(CCC);
                if (config == null) {
                    return true;
                }

                // enable/disable locally
                bluetoothGatt.setCharacteristicNotification(dataCharacteristic, start);
                // enable/disable remotely
                config.setValue(start ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(config);
                return false;
            }
        };
    }

    private BluetoothGattCharacteristic getCharacteristic(BluetoothGatt bluetoothGatt, String uuid) {
        final UUID serviceUuid = UUID.fromString(getServiceUUID());
        final UUID characteristicUuid = UUID.fromString(uuid);

        final BluetoothGattService service = bluetoothGatt.getService(serviceUuid);
        return service.getCharacteristic(characteristicUuid);
    }
}