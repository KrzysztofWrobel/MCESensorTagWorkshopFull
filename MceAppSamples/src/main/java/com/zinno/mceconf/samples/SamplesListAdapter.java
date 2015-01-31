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
            case HELLOWORLD:
                break;
            case MAGNETOMETER:
                break;
            case ACCELEROMETER:
                intent = new Intent(this.context, DiceActivity.class);
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_NAME, "Dice Sensor");
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_ADDRESS, "BC:6A:29:AB:81:A9");
                break;

            case BUTTON_GAME:
                intent = new Intent(this.context, RunningButtonGameActivity.class);
                ArrayList<String> deviceNames = new ArrayList<>();
                deviceNames.add("Player 1 pad");
                deviceNames.add("Player 2 pad");
                ArrayList<String> deviceAddresses = new ArrayList<>();
                deviceAddresses.add("BC:6A:29:AB:45:79");
                deviceAddresses.add("BC:6A:29:AB:81:A9");
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_NAMES, deviceNames);
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_ADDRESSES, deviceAddresses);
                break;
            case GYROSCOPE:
                intent = new Intent(this.context, GyroscopeActivity.class);
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_NAME, "SensorTag");
                intent.putExtra(BleServiceBindingActivity.EXTRAS_DEVICE_ADDRESS, "34:B1:F7:D5:04:01");
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
