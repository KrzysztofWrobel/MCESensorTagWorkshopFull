package com.zinno.mceconf.samples;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.zinno.sensortag.BleService;
import com.zinno.sensortag.BleServiceBindingActivity;
import com.zinno.sensortag.sensor.TiKeysSensor;
import com.zinno.sensortag.sensor.TiSensor;
import com.zinno.sensortag.sensor.TiSensors;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TwitterActivity extends BleServiceBindingActivity {
    private static final String TAG = TwitterActivity.class.getSimpleName();

    @InjectView(R.id.leftButton)
    Button leftButton;

    @InjectView(R.id.rightButton)
    Button rightButton;

    @InjectView(R.id.morseTextView)
    TextView morseTextView;

    @InjectView(R.id.charsTextView)
    TextView charsTextView;

    @InjectView(R.id.helpGridView)
    GridView gridView;

    @InjectView(R.id.action_bar)
    Toolbar toolbar;

    TiSensor<?> keysSensor;

    boolean sensorEnabled = false;

    ButtonController buttonController;
    CharacterDetector characterDetector;
    ToneDetector toneDetector;
    HelpAdapter helpAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        keysSensor = TiSensors.getSensor(TiKeysSensor.UUID_SERVICE);

        buttonController = new ButtonController();
        characterDetector = new CharacterDetector();
        toneDetector = new ToneDetector();
        helpAdapter = new HelpAdapter(this);

        setContentView(R.layout.activity_twitter);

        ButterKnife.inject(this);

        gridView.setAdapter(helpAdapter);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterActivity.this.finish();
            }
        });
        toolbar.setTitle(R.string.twitter_sample_name);
    }

    @Override
    public void onDisconnected(String deviceAddress) {
        finish();
    }

    @Override
    public void onServiceDiscovered(String deviceAddress) {
        sensorEnabled = true;

        getBleService().enableSensor(getDeviceAddress(), keysSensor, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        BleService bleService = getBleService();
        if (bleService != null && sensorEnabled) {
            bleService.enableSensor(getDeviceAddress(), keysSensor, false);
        }

        super.onPause();
    }

    @Override
    public void onDataAvailable(String deviceAddress, String serviceUuid, String characteristicUUid, String text, byte[] data) {
        Log.d(TAG, String.format("ServiceUUID: %s, CharacteristicUUIS: %s", serviceUuid, characteristicUUid));
        Log.d(TAG, String.format("Data: %s", text));

        TiSensor<?> tiSensor = TiSensors.getSensor(serviceUuid);
        final TiKeysSensor tiKeysSensor = (TiKeysSensor) tiSensor;
        TiKeysSensor.SimpleKeysStatus simpleKeysStatus = tiKeysSensor.getData();

        buttonController.onKeysStatusChange(simpleKeysStatus);
        toneDetector.onKeysStatusChange(simpleKeysStatus);
    }

    public class ButtonController {
        public void onKeysStatusChange(TiKeysSensor.SimpleKeysStatus keysStatus) {
            switch (keysStatus) {
                case OFF_OFF:
                    leftButton.setPressed(false);
                    rightButton.setPressed(false);
                    break;
                case OFF_ON:
                    leftButton.setPressed(false);
                    rightButton.setPressed(true);
                    break;
                case ON_OFF:
                    leftButton.setPressed(true);
                    rightButton.setPressed(false);
                    break;
                case ON_ON:
                    leftButton.setPressed(true);
                    rightButton.setPressed(true);
                    break;
            }
        }
    }

    public class ToneDetector {
        long pressTimestamp = -1;
        TiKeysSensor.SimpleKeysStatus keysStatus;

        public void onKeysStatusChange(TiKeysSensor.SimpleKeysStatus keysStatus) {
            switch (keysStatus) {
                case OFF_OFF:
                    // button up!
                    detectTone();
                    break;
                case OFF_ON:
                    // delete character
                    characterDetector.deleteToneOrCharacter();
                    break;
                case ON_OFF:
                    // count press time
                    pressTimestamp = System.currentTimeMillis();
                    break;
                case ON_ON:
                    // ignore
                    break;
            }
        }

        private void detectTone() {
            if (pressTimestamp == -1) return;
            long now = System.currentTimeMillis();
            long diff = now - pressTimestamp;
            System.out.println("diff=" + diff);
            Tone tone = diff < 400 ? Tone.DOT : Tone.DASH;
            characterDetector.onNewTone(tone);
            pressTimestamp = -1;
        }
    }

    public class CharacterDetector {
        private static final long DETECTION_THRESHOLD = 2000;

        List<MorseCharacter> characters = new ArrayList<>();
        List<Tone> tones = new ArrayList<>();
        Handler handler = new Handler();

        public List<Tone> getTones() {
            return tones;
        }

        public void onNewTone(Tone tone) {
            handler.removeCallbacks(detectionRunnable);

            tones.add(tone);
            updateUI();

            handler.postDelayed(detectionRunnable, DETECTION_THRESHOLD);
        }

        public void deleteToneOrCharacter() {
            handler.removeCallbacks(detectionRunnable);

            if (tones.size() > 0) {
                tones.remove(tones.size() - 1);
            } else if (characters.size() > 0) {
                characters.remove(characters.size() - 1);
            }

            updateUI();

            handler.postDelayed(detectionRunnable, DETECTION_THRESHOLD);
        }

        Runnable detectionRunnable = new Runnable() {
            @Override
            public void run() {
                List<MorseCharacter> matching = MorseCharacter.getMatching(tones);

                if (matching.size() == 0) {
                    Toast.makeText(TwitterActivity.this, "Unknown morse code", Toast.LENGTH_LONG).show();

                    tones.clear();
                } else if (matching.size() >= 1) {
                    for (MorseCharacter morseCharacter : matching) {
                        if (morseCharacter.equals(tones)) {
                            tones.clear();
                            characters.add(morseCharacter);
                        }
                    }
                }

                updateUI();
            }
        };

        public void updateUI() {
            String tmp = "";
            for (Tone tone : tones) {
                tmp += tone.character + " ";
            }
            if (TextUtils.isEmpty(tmp)) {
                morseTextView.setVisibility(View.GONE);
            } else {
                morseTextView.setVisibility(View.VISIBLE);
                morseTextView.setText(tmp);
            }

            tmp = "";
            for (MorseCharacter morseCharacter : characters) {
                tmp += morseCharacter.name();
            }
            if (TextUtils.isEmpty(tmp)) {
                charsTextView.setVisibility(View.GONE);
            } else {
                charsTextView.setVisibility(View.VISIBLE);
                charsTextView.setText(tmp);
            }

            helpAdapter.notifyDataSetChanged();
        }
    }

    public class HelpAdapter extends ArrayAdapter<MorseCharacter> {
        @Override
        public int getCount() {
            return MorseCharacter.values().length;
        }

        public HelpAdapter(Context context) {
            super(context, R.layout.view_morse_character);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.view_morse_character, null);
            }

            MorseCharacter morseCharacter = MorseCharacter.values()[position];

            TextView textView;

            textView = (TextView) convertView.findViewById(R.id.letterTextView);
            textView.setText(morseCharacter.name());

            textView = (TextView) convertView.findViewById(R.id.codeTextView);
            String tmp = "";
            for (char sign : morseCharacter.tones.toCharArray()) {
                tmp += sign + " ";
            }
            textView.setText(tmp);

            if (morseCharacter.matches(characterDetector.getTones())) {
                convertView.setVisibility(View.VISIBLE);
            } else {
                convertView.setVisibility(View.GONE);
            }

            return convertView;
        }
    }


    public enum MorseCharacter {
        A("._"),
        B("_..."),
        C("_._."),
        D("_.."),
        E("."),
        F(".._."),
        G("__."),
        H("...."),
        I(".."),
        J(".___"),
        K("_._"),
        L("._.."),
        M("__"),
        N("_."),
        O("___"),
        P(".__."),
        Q("__._"),
        R("._."),
        S("..."),
        T("_"),
        U(".._"),
        V("..._"),
        W(".__"),
        X("_.._"),
        Y("_.__"),
        Z("__..");

        String tones;

        MorseCharacter(String tones) {
            this.tones = tones;
        }

        public static List<MorseCharacter> getMatching(List<Tone> tones) {
            List<MorseCharacter> matching = new ArrayList<MorseCharacter>();

            for (MorseCharacter morseCharacter : MorseCharacter.values()) {
                if (morseCharacter.matches(tones)) {
                    matching.add(morseCharacter);
                }
            }

            return matching;
        }

        public boolean matches(List<Tone> tonesList) {
            String tonesAsString = "";
            for (Tone tone : tonesList) {
                tonesAsString += tone.character;
            }

            return tones.startsWith(tonesAsString);
        }

        public boolean equals(List<Tone> tonesList) {
            String tonesAsString = "";
            for (Tone tone : tonesList) {
                tonesAsString += tone.character;
            }

            return tones.equals(tonesAsString);
        }
    }

    public enum Tone {
        DOT("."),
        DASH("_");

        String character;

        Tone(String character) {
            this.character = character;
        }
    }
}
