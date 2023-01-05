package com.example.geslock.tools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public abstract class MyAnimationScaler {

    private static SharedPreferences pref;
    private static int selectionIndex;
    private static float scale;

    public static int getDuration(int standardDuration, Activity activity) {
        setScale(getCurrentIndex(activity));
        return (int) (standardDuration * scale);
    }

    public static float getSpringStiffness(float defaultStiffness, Activity activity) {
        setScale(getCurrentIndex(activity));
        return scale == 0 ? 524 : defaultStiffness / (scale * 2);
    }

    public static int getCurrentIndex(Activity activity) {
        if (pref == null)
            pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE);
        return pref.getInt("animation", MyDefaultPref.getDefaultInt("animation"));
    }

    public static void setScale(int currentIndex) {
        if (selectionIndex != currentIndex) {
            selectionIndex = currentIndex;
            switch (selectionIndex) {
                case 0:
                    scale = 0;
                    break;
                case 1:
                    scale = 0.5F;
                    break;
                case 2:
                    scale = 1;
                    break;
                case 3:
                    scale = 1.5F;
                    break;
                case 4:
                    scale = 2;
                    break;
                case 5:
                    scale = 5;
                    break;
            }
        }
    }

    public static float getScale() {
        return scale;
    }
}
