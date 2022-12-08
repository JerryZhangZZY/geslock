package com.example.geslock.tools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public abstract class MyAnimationScaler {

    private static SharedPreferences pref;
    private static int selectionIndex;
    private static float scale;

    public static int getDuration(int standardDuration, Activity activity) {
        if (pref == null)
            pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE);
        int currentIndex = pref.getInt("animation", 2);

        // animation pref not change
        if (selectionIndex == currentIndex)
            return (int) (standardDuration * scale);

        // new pref
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
                return standardDuration;
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
        return (int) (standardDuration * scale);
    }
}
