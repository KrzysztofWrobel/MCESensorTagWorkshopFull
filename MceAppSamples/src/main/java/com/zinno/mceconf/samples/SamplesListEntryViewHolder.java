package com.zinno.mceconf.samples;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SamplesListEntryViewHolder extends RecyclerView.ViewHolder {
    @InjectView(R.id.nameTextView)
    public TextView nameTextView;

    public SamplesListEntryViewHolder(View v) {
        super(v);

        ButterKnife.inject(this, v);
    }
}