package com.example.geslock.tools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public abstract class MyVibrator {

    private static Vibrator vibrator;
    private static SharedPreferences pref;

    public static void tick(Activity activity) {
        if (vibrator == null) {
            vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);
            pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE);
        }
        if (pref.getBoolean("vibration", true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.EFFECT_TICK);
            } else {
                vibrator.vibrate(20);
            }
        }
    }
}
