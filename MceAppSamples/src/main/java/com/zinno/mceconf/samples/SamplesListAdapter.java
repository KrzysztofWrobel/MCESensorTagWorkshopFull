package com.zinno.mceconf.samples;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joanzapata.android.iconify.Iconify;

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
                break;
            case GYROSCOPE:
                intent = new Intent(this.context, GyroscopeActivity.class);
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
