package com.zinno.mceconf.samples;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joanzapata.android.iconify.Iconify;
import com.zinno.sensortag.BleServiceBindingActivity;

import java.util.ArrayList;

public class SamplesListAdapter extends RecyclerView.Adapter<SamplesListEntryViewHolder> {
    Context context;

    public SamplesListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public SamplesListEntryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_sampleslist_entry, viewGroup, false);
        return new SamplesListEntryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SamplesListEntryViewHolder viewHolder, int position) {
        final Samples sample = Samples.values()[position];

        viewHolder.nameTextView.setText(sample.nameId);

        viewHolder.iconTextView.setText(sample.iconId);
        Iconify.addIcons(viewHolder.iconTextView);

        viewHolder.iconTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSampleClick(sample);
            }
        });
    }

    private void onSampleClick(Samples sample) {
        Intent intent = null;

        switch (sample) {
            case TWITTER:
                intent = new Intent(this.context, TwitterActivity.class);
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_NAME, "Button Sensor");
//                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_ADDRESS, "34:B1:F7:D5:04:01");
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_ADDRESS, "BC:6A:29:AC:7D:10");
                break;
            case MAGNETOMETER:
                intent = new Intent(this.context, MagnetometerActivity.class);
                ArrayList<String> deviceNames = new ArrayList<>();
                deviceNames.add("Sensor Tag");
                ArrayList<String> deviceAddresses = new ArrayList<>();
                deviceAddresses.add("BC:6A:29:AB:81:A9");
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_NAMES, deviceNames);
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_ADDRESSES, deviceAddresses);
                break;
            case ACCELEROMETER:
                intent = new Intent(this.context, DiceActivity.class);
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_NAME, "Dice Sensor");
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_ADDRESS, "BC:6A:29:AB:81:A9");
                break;

            case BUTTON_GAME:
                intent = new Intent(this.context, RunningButtonGameActivity.class);
                ArrayList<String> deviceNames1 = new ArrayList<>();
                deviceNames1.add("Player 1 pad");
                deviceNames1.add("Player 2 pad");
                ArrayList<String> deviceAddresses1 = new ArrayList<>();
                deviceAddresses1.add("BC:6A:29:AB:81:A9");
                deviceAddresses1.add("BC:6A:29:AB:45:79");
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_NAMES, deviceNames1);
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_ADDRESSES, deviceAddresses1);
                break;
            case GYROSCOPE:
                intent = new Intent(this.context, GyroscopeActivity.class);
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_NAME, "SensorTag");
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_ADDRESS, "34:B1:F7:D5:04:01");
                break;
            case BAROMETER:
                intent = new Intent(this.context, PressureActivity.class);
                ArrayList<String> deviceNames2 = new ArrayList<>();
                deviceNames2.add("Sensor Tag");
                ArrayList<String> deviceAddresses2 = new ArrayList<>();
                deviceAddresses2.add("BC:6A:29:AB:45:79");
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_NAMES, deviceNames2);
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_ADDRESSES, deviceAddresses2);
                break;
        }

        if (intent != null) {
            context.startActivity(intent);
        }
    }

    @Override
    public int getItemCount() {
        return Samples.values().length;
    }
}
