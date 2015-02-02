package com.zinno.mceconf.samples;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.zinno.mceconf.samples.adapters.BleDevicesAdapter;
import com.zinno.mceconf.samples.dialogs.EnableBluetoothDialog;
import com.zinno.mceconf.samples.dialogs.ErrorDialog;
import com.zinno.sensortag.ble.BleDevicesScanner;
import com.zinno.sensortag.ble.BleUtils;


/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class MCEDeviceScanActivity extends ListActivity
        implements ErrorDialog.ErrorDialogListener,
        EnableBluetoothDialog.EnableBluetoothDialogListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 500;

    Toolbar toolbar;

    private BleDevicesAdapter leDeviceListAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private BleDevicesScanner scanner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_devices);

        setContentView(R.layout.device_scan_activity);
        final View emptyView = findViewById(R.id.empty_view);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        toolbar.inflateMenu(R.menu.gatt_scan);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_scan:
                        leDeviceListAdapter.clear();
                        if (scanner != null)
                            scanner.start();
                        refreshMenu();
                        break;
                    case R.id.menu_stop:
                        if (scanner != null)
                            scanner.stop();
                        refreshMenu();
                        break;
                }
                return true;
            }
        });
        
        getListView().setEmptyView(emptyView);

        final int bleStatus = BleUtils.getBleStatus(getBaseContext());
        switch (bleStatus) {
            case BleUtils.STATUS_BLE_NOT_AVAILABLE:
                ErrorDialog.newInstance(R.string.dialog_error_no_ble).show(getFragmentManager(), ErrorDialog.TAG);
                return;
            case BleUtils.STATUS_BLUETOOTH_NOT_AVAILABLE:
                ErrorDialog.newInstance(R.string.dialog_error_no_bluetooth).show(getFragmentManager(), ErrorDialog.TAG);
                return;
            default:
                bluetoothAdapter = BleUtils.getBluetoothAdapter(getBaseContext());
        }

        if (bluetoothAdapter == null)
            return;

        // initialize scanner
        scanner = new BleDevicesScanner(bluetoothAdapter, new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                leDeviceListAdapter.addDevice(device, rssi);
                leDeviceListAdapter.notifyDataSetChanged();
            }
        });
        scanner.setScanPeriod(SCAN_PERIOD);
    }

    public boolean refreshMenu() {
        Menu menu = toolbar.getMenu();

        if (scanner == null || !scanner.isScanning()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.ab_indeterminate_progress);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bluetoothAdapter == null)
            return;

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!bluetoothAdapter.isEnabled()) {
            final Fragment f = getFragmentManager().findFragmentByTag(EnableBluetoothDialog.TAG);
            if (f == null)
                new EnableBluetoothDialog().show(getFragmentManager(), EnableBluetoothDialog.TAG);
            return;
        }

        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            } else {
                init();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (scanner != null)
            scanner.stop();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = leDeviceListAdapter.getDevice(position);
        if (device == null)
            return;

    }

    private void init() {
        if (leDeviceListAdapter == null) {
            leDeviceListAdapter = new BleDevicesAdapter(getBaseContext());
            setListAdapter(leDeviceListAdapter);
        }

        scanner.start();
        refreshMenu();
    }

    @Override
    public void onEnableBluetooth(EnableBluetoothDialog f) {
        bluetoothAdapter.enable();
        init();
    }

    @Override
    public void onCancel(EnableBluetoothDialog f) {
        finish();
    }

    @Override
    public void onDismiss(ErrorDialog f) {
        finish();
    }
}