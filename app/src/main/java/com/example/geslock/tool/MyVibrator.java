package com.example.geslock.tool;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public abstract class MyVibrator {
    public static void tick(Activity activity) {
        SharedPreferences pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE);
        Vibrator vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (pref.getBoolean("vibration", true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.EFFECT_TICK);
            } else {
                vibrator.vibrate(20);
            }
        }
    }
}
