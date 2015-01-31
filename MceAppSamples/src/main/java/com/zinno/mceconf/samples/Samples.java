package com.zinno.mceconf.samples;

public enum Samples {
    HELLOWORLD(R.string.hello_world_sample_name, R.string.hello_world_sample_icon),
    MAGNETOMETER(R.string.magnetometer_sample_name, R.string.magnetometer_sample_icon),
    ACCELEROMETER(R.string.accelerometer_sample_name, R.string.accelerometer_sample_icon),
    BUTTON_GAME(R.string.button_game_sample_name, R.string.button_game_sample_icon),
    GYROSCOPE(R.string.gyroscope_sample_name, R.string.gyroscope_sample_icon);

    int nameId, iconId;

    Samples(int nameId, int iconId) {
        this.nameId = nameId;
        this.iconId = iconId;
    }
}
