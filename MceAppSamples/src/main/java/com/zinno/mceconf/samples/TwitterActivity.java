package com.zinno.mceconf.samples;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zinno.sensortag.BleService;
import com.zinno.sensortag.BleServiceBindingActivity;
import com.zinno.sensortag.sensor.TiKeysSensor;
import com.zinno.sensortag.sensor.TiSensor;
import com.zinno.sensortag.sensor.TiSensors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TwitterActivity extends BleServiceBindingActivity {
    private static final String TAG = TwitterActivity.class.getSimpleName();

    @InjectView(R.id.leftButton)
    Button leftButton;

    @InjectView(R.id.rightButton)
    Button rightButton;

//    @InjectView(R.id.infoContainer0)
//    LinearLayout infoContainer0;
//
//    @InjectView(R.id.infoContainer1)
//    Button infoContainer1;
//
//    @InjectView(R.id.infoContainer2)
//    Button infoContainer2;

    @InjectView(R.id.morseTextView)
    TextView morseTextView;

    @InjectView(R.id.charsTextView)
    TextView charsTextView;

    TiSensor<?> keysSensor;

    boolean sensorEnabled = false;

    ButtonController buttonController;
    CharacterDetector characterDetector;
    ToneDetector toneDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        keysSensor = TiSensors.getSensor(TiKeysSensor.UUID_SERVICE);

        buttonController = new ButtonController();
        characterDetector = new CharacterDetector();
        toneDetector = new ToneDetector();

        setContentView(R.layout.activity_twitter);

        ButterKnife.inject(this);
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
        private static final long DETECTION_TRESHOLD = 1500;

        HashMap<String, String> morseCode = new HashMap<String, String>() {{
            put("..", "I");

            put(".___", "J");
            put("_._", "K");
            put("._..", "L");
            put("__", "M");
            put("_.", "N");
            put("___", "O");
            put(".__.", "P");
            put("__._", "Q");
            put("._.", "R");

            put("...", "S");
            put("_", "T");
            put(".._", "U");
            put("..._", "V");
            put(".__", "W");
            put("_.._", "X");
            put("_.__", "Y");
            put("__..", "Z");
        }};

        List<MorseCharacter> characters = new ArrayList<>();
        List<Tone> tones = new ArrayList<>();
        Handler handler = new Handler();

        public void onNewTone(Tone tone) {
            handler.removeCallbacks(detectionRunnable);

            tones.add(tone);
            updateUI();

            handler.postDelayed(detectionRunnable, DETECTION_TRESHOLD);

            Log.d(TAG, "tones=" + tones.size() + " " + characters.size());

            /*
            List<MorseCharacter> matching = MorseCharacter.getMatching(tones);

            if (matching.size() == 0) {
                // toast that there is an error

                    morseTextView.setText("");
                tones.clear();
            } else if (matching.size() == 1) {
                MorseCharacter morseCharacter = matching.get(0);
                charsTextView.setText(charsTextView.getText() + morseCharacter.name());

                morseTextView.setText("");
                tones.clear();
            } else {
                // show help
            }
            */
        }

        public void deleteToneOrCharacter() {
            Log.d(TAG, "before tones=" + tones.size() + " " + characters.size());

            if (tones.size() > 0) {
                tones.remove(tones.size() - 1);
            } else if (characters.size() > 0) {
                characters.remove(characters.size() - 1);
            }

            handler.removeCallbacks(detectionRunnable);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUI();
                }
            });

            Log.d(TAG, "after tones=" + tones.size() + " " + characters.size());
        }

        Runnable detectionRunnable = new Runnable() {
            @Override
            public void run() {
                List<MorseCharacter> matching = MorseCharacter.getMatching(tones);

                if (matching.size() == 0) {
                    Toast.makeText(TwitterActivity.this, "Unknown more code", Toast.LENGTH_LONG).show();
                    tones.clear();
                    updateUI();
                } else if (matching.size() == 1) {
                    MorseCharacter morseCharacter = matching.get(0);

                    Log.d(TAG, "got=" + morseCharacter);

                    tones.clear();
                    characters.add(morseCharacter);

                    updateUI();
                } else {

                }
            }
        };

        public void updateUI() {
            String tmp = "";
            for (Tone tone: tones) {
                tmp += tone.character;
            }
            if (TextUtils.isEmpty(tmp)) {
                morseTextView.setVisibility(View.GONE);
            } else {
                morseTextView.setVisibility(View.VISIBLE);
                morseTextView.setText(tmp);
            }

            tmp = "";
            for (MorseCharacter morseCharacter: characters) {
                tmp += morseCharacter.name();
            }
            if (TextUtils.isEmpty(tmp)) {
                charsTextView.setVisibility(View.GONE);
            } else {
                charsTextView.setVisibility(View.VISIBLE);
                charsTextView.setText(tmp);
            }
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
        I("..");

        String tones;

        MorseCharacter(String tones) {
            this.tones = tones;
        }

        public static List<MorseCharacter> getMatching(List<Tone> tones) {
            String tonesAsString = "";
            for (Tone tone : tones) {
                tonesAsString += tone.character;
            }

            List<MorseCharacter> matching = new ArrayList<MorseCharacter>();
            for (MorseCharacter morseCharacter: MorseCharacter.values()) {
                if (morseCharacter.tones.startsWith(tonesAsString)) {
                    matching.add(morseCharacter);
                }
            }

            return matching;
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
