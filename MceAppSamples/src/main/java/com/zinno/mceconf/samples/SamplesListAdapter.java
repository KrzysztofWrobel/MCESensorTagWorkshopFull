package com.zinno.mceconf.samples;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joanzapata.android.iconify.Iconify;

public class SamplesListAdapter extends RecyclerView.Adapter<SamplesListEntryViewHolder> {
    public SamplesListAdapter() {
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
    }

    @Override
    public int getItemCount() {
        return Samples.values().length;
    }
}
