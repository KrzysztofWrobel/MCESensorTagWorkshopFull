package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SamplesListFragment extends Fragment {
    SamplesListAdapter samplesListAdapter;

    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;

    public static SamplesListFragment newInstance() {
        return new SamplesListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        samplesListAdapter = new SamplesListAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sampleslist, container, false);

        ButterKnife.inject(this, v);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(samplesListAdapter);

        return v;
    }
}