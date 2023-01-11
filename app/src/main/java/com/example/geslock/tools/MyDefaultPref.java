package com.example.geslock.tools;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class MyDefaultPref {

    private static final Map<String, Object> defaultValues = new HashMap<>();
    private static final boolean init = false;

    // appearance
    private static final int icon = 0;
    private static final boolean cross = true;
    private static final int theme = -1;
    private static final int language = 2;

    // general
    private static final boolean itemCount = true;

    // interaction
    private static final int travel = 1;
    private static final boolean vibration = true;

    // advanced
    private static final int animation = 2;
    private static final float overshoot = 0.3F;
    private static final float smRatio = 0.2F;
    private static final float tmRatio = 0.04F;

    // files
    private static final int sort = 0;

    public static void init() {
        defaultValues.put("icon", icon);
        defaultValues.put("cross", cross);
        defaultValues.put("theme", theme);
        defaultValues.put("language", language);
        defaultValues.put("item-count", itemCount);
        defaultValues.put("travel", travel);
        defaultValues.put("vibration", vibration);
        defaultValues.put("animation", animation);
        defaultValues.put("overshoot", overshoot);
        defaultValues.put("sm-ratio", smRatio);
        defaultValues.put("tm-ratio", tmRatio);
    }

    public static boolean getDefaultBoolean(String key) {
        if (!init) {
            init();
        }
        try {
            return (boolean) defaultValues.get(key);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static int getDefaultInt(String key) {
        if (!init) {
            init();
        }
        try {
            return (int) defaultValues.get(key);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static float getDefaultFloat(String key) {
        if (!init) {
            init();
        }
        try {
            return (float) defaultValues.get(key);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void resetToDefault(SharedPreferences.Editor editor) {
        editor.putInt("icon", icon);
        editor.putBoolean("cross", cross);
        editor.putInt("theme", theme);
        editor.putInt("language", language);
        editor.putBoolean("item-count", itemCount);
        editor.putInt("travel", travel);
        editor.putBoolean("vibration", vibration);
        editor.putInt("animation", animation);
        editor.putFloat("overshoot", overshoot);
        editor.putFloat("sm-ratio", smRatio);
        editor.putFloat("tm-ratio", tmRatio);
        editor.commit();
    }
}
