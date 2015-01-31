package com.zinno.sensortag.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;

import com.zinno.sensortag.sensor.TiSensor;

import java.util.LinkedList;

/**
 * Created by steven on 9/3/13.
 */
public class BleGattExecutor extends BluetoothGattCallback {

    public static abstract class ServiceAction {


        public enum ActionType {
            NONE,
            READ,
            NOTIFY,
            WRITE
        }

        public static final ServiceAction NULL = new ServiceAction(null, ActionType.NONE) {
            @Override
            public boolean execute(BluetoothGatt bluetoothGatt) {
                // it is null action. do nothing.
                return true;
            }
        };

        private final ActionType type;

        private BluetoothGatt gatt;

        public ServiceAction(BluetoothGatt gatt, ActionType type) {
            this.gatt = gatt;
            this.type = type;
        }

        public BluetoothGatt getGatt() {
            return gatt;
        }

        public ActionType getType() {
            return type;
        }

        /**
         * Executes action.
         *
         * @param bluetoothGatt
         * @return true - if action was executed instantly. false if action is waiting for
         * feedback.
         */
        public abstract boolean execute(BluetoothGatt bluetoothGatt);
    }

    private final LinkedList<BleGattExecutor.ServiceAction> queue = new LinkedList<ServiceAction>();
    private volatile ServiceAction currentAction;

    public void update(BluetoothGatt gatt, final TiSensor sensor) {
        queue.add(sensor.update(gatt));
    }

    public void enable(BluetoothGatt gatt, TiSensor sensor, boolean enable) {
        final ServiceAction[] actions = sensor.enable(gatt, enable);
        for (ServiceAction action : actions) {
            this.queue.add(action);
        }
    }

    public void executeNextAction() {
        if (currentAction != null) {
            return;
        }

        boolean next = !queue.isEmpty();
        while (next) {
            final BleGattExecutor.ServiceAction action = queue.pop();
            currentAction = action;
            if (!action.execute(currentAction.getGatt()))
                break;

            currentAction = null;
            next = !queue.isEmpty();
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);

        // wait for onCharacteristicWrite for write action before execution of any other actions
        if (currentAction != null && currentAction.getType() == ServiceAction.ActionType.WRITE) {
            return;
        }

        currentAction = null;
        executeNextAction();
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);

        currentAction = null;
        executeNextAction();
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status) {
        // wait for onCharacteristicWrite for write action before execution of any other actions
        if (currentAction != null && currentAction.getType() == ServiceAction.ActionType.WRITE) {
            return;
        }

        currentAction = null;
        executeNextAction();
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            for (ServiceAction serviceAction : queue) {
                if (serviceAction.gatt.equals(gatt)) {
                    queue.remove(serviceAction);
                }
            }
        }
    }
}
