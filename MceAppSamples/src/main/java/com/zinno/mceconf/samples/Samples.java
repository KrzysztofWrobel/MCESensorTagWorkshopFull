package com.zinno.mceconf.samples;

public enum Samples {
    TWITTER(R.string.twitter_sample_name, R.string.twitter_sample_icon),
    BUTTON_GAME(R.string.button_game_sample_name, R.string.button_game_sample_icon),
    ACCELEROMETER(R.string.accelerometer_sample_name, R.string.accelerometer_sample_icon),
    SAFE_LOCK(R.string.safe_lock_sample_name, R.string.safe_lock_sample_icon),
    TEMPERATURE(R.string.temperature_sample_name, R.string.temperature_sample_icon),
    MAGNETOMETER(R.string.magnetometer_sample_name, R.string.magnetometer_sample_icon),
    BAROMETER(R.string.barometer_sample_name, R.string.barometer_sample_icon),
    GYROSCOPE(R.string.gyroscope_sample_name, R.string.gyroscope_sample_icon),
    HYGROMETER(R.string.hygrometer_sample_name, R.string.hygrometer_sample_icon);

    int nameId, iconId;

    Samples(int nameId, int iconId) {
        this.nameId = nameId;
        this.iconId = iconId;
    }
}
