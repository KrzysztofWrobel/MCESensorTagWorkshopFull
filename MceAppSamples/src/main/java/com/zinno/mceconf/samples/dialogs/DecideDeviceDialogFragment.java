package com.zinno.mceconf.samples.dialogs;

import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zinno.mceconf.samples.LocalStorage;
import com.zinno.mceconf.samples.R;

/**
 * Created by krzysztofwrobel on 02/02/15.
 */
public class DecideDeviceDialogFragment extends DialogFragment {

    public static final String ARGS_DEVICE = "device";
    private BluetoothDevice device;

    public static DecideDeviceDialogFragment newInstance(BluetoothDevice device) {
        DecideDeviceDialogFragment fragment = new DecideDeviceDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_DEVICE, device);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        device = getArguments().getParcelable(ARGS_DEVICE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.decide_dialog_fragment, container, false);
        Button firstButton = (Button) rootView.findViewById(R.id.b_first_device);
        firstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalStorage.setAsDevice(getActivity(), device, 0);
                dismiss();
            }
        });
        Button secondButton = (Button) rootView.findViewById(R.id.b_second_device);
        secondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalStorage.setAsDevice(getActivity(), device, 1);
                dismiss();
            }
        });
        getDialog().setTitle(getResources().getString(R.string.use_chosen_device_as));

        return rootView;
    }


}
