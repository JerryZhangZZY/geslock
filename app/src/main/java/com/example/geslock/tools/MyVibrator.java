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
    private static final int duration = 20;

    public static void tick(Activity activity) {
        init(activity);
        if (pref.getBoolean("vibration", MyDefaultPref.getDefaultBoolean("vibration"))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(duration);
            }
        }
    }

    public static void shake(Activity activity) {
        init(activity);
        if (pref.getBoolean("vibration", MyDefaultPref.getDefaultBoolean("vibration"))) {
            new Thread(() -> {
                try {
                    float scale = MyAnimationScaler.getScale();
                    if (scale == 0F) {
                        scale = 0.5F;
                    } else if (scale == 5F) {
                        scale = 3F;
                    }
                    int interval = (int) (100 * scale);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createOneShot(duration * 2, 255));
                        Thread.sleep(interval);
                        vibrator.vibrate(VibrationEffect.createOneShot((long) (duration * 1.5), 200));
                        Thread.sleep(interval);
                        vibrator.vibrate(VibrationEffect.createOneShot(duration, 128));
                        Thread.sleep(interval);
                        vibrator.vibrate(VibrationEffect.createOneShot(duration / 2, 64));

                    } else {
                        vibrator.vibrate(20);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }).start();
        }
    }

    public static void init(Activity activity) {
        if (vibrator == null) {
            vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);
            pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE);
        }
    }
}
