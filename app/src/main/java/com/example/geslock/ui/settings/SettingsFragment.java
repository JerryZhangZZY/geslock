package com.example.geslock.ui.settings;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.geslock.R;
import com.example.geslock.tool.MyVibrator;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private ImageButton[] rockerIcons;
    private Drawable border;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchCross;
    private Spinner spinnerTheme;
    private Spinner spinnerLanguage;
    private Spinner spinnerTravel;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchVibration;
    private EditText editTextSPRatio;

    private final Object lock = new Object();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();

        assert activity != null;
        SharedPreferences pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        rockerIcons = new ImageButton[3];
        rockerIcons[0] = activity.findViewById(R.id.imgBtnSoccer);
        rockerIcons[1] = activity.findViewById(R.id.imgBtnDonut);
        rockerIcons[2] = activity.findViewById(R.id.imgBtnBasketball);

        border = ResourcesCompat.getDrawable(getResources(), R.drawable.round_btn_border, null);

        for (int index = 0; index < rockerIcons.length; index++) {
            rockerIcons[index].setBackground(pref.getInt("icon", 0) == index ? border : null);
            int finalIndex = index;
            rockerIcons[index].setOnClickListener(view -> {
                synchronized (lock) {
                    MyVibrator.tick(requireActivity());
                    for (ImageButton btn : rockerIcons)
                        btn.setBackground(null);
                    ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(border, PropertyValuesHolder.ofInt("alpha", 0, 255));
                    animator.setTarget(border);
                    animator.setDuration(200);
                    animator.start();
                    rockerIcons[finalIndex].setBackground(border);
                    editor.putInt("icon", finalIndex);
                    editor.apply();
                }
            });
        }

        switchCross = activity.findViewById(R.id.switchCross);
        switchCross.setChecked(pref.getBoolean("cross", true));
        switchCross.setOnCheckedChangeListener((compoundButton, b) -> {
            editor.putBoolean("cross", b);
            editor.apply();
        });

        spinnerTheme = activity.findViewById(R.id.spinnerTheme);
        switch (pref.getInt("theme", MODE_NIGHT_FOLLOW_SYSTEM)) {
            case MODE_NIGHT_NO:
                spinnerTheme.setSelection(0);
                break;
            case MODE_NIGHT_YES:
                spinnerTheme.setSelection(1);
                break;
            case MODE_NIGHT_FOLLOW_SYSTEM:
                spinnerTheme.setSelection(2);
                break;
        }
        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                synchronized (lock) {
                    switch (i) {
                        case 0:
                            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                            editor.putInt("theme", MODE_NIGHT_NO);
                            editor.apply();
                            break;
                        case 1:
                            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                            editor.putInt("theme", MODE_NIGHT_YES);
                            editor.apply();
                            break;
                        case 2:
                            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                            editor.putInt("theme", MODE_NIGHT_FOLLOW_SYSTEM);
                            editor.apply();
                            break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        spinnerLanguage = activity.findViewById(R.id.spinnerLanguage);
        spinnerLanguage.setSelection(pref.getInt("language", 2));
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        final boolean[] enabledFlag = {false};
        spinnerLanguage.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                enabledFlag[0] = true;
                return false;
            }
        });
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (enabledFlag[0]) {
                    synchronized (lock) {
                        switch (i) {
                            case 0:
                                configuration.setLocale(Locale.ENGLISH);
                                resources.updateConfiguration(configuration, displayMetrics);
                                editor.putInt("language", 0);
                                editor.apply();
                                activity.recreate();
                                break;
                            case 1:
                                configuration.setLocale(Locale.CHINESE);
                                resources.updateConfiguration(configuration, displayMetrics);
                                editor.putInt("language", 1);
                                editor.apply();
                                activity.recreate();
                                break;
                            case 2:
                                configuration.setLocale(Locale.getDefault());
                                resources.updateConfiguration(configuration, displayMetrics);
                                editor.putInt("language", 2);
                                editor.apply();
                                activity.recreate();
                                break;
                        }
                    }
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        spinnerTravel = activity.findViewById(R.id.spinnerTravel);
        spinnerTravel.setSelection(pref.getInt("drag-dist", 1));
        spinnerTravel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        editor.putInt("drag-dist", 0);
                        editor.apply();
                        break;
                    case 1:
                        editor.putInt("drag-dist", 1);
                        editor.apply();
                        break;
                    case 2:
                        editor.putInt("drag-dist", 2);
                        editor.apply();
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        switchVibration = activity.findViewById(R.id.switchVibration);
        switchVibration.setChecked(pref.getBoolean("vibration", true));
        switchVibration.setOnCheckedChangeListener((compoundButton, b) -> {
            editor.putBoolean("vibration", b);
            editor.apply();
        });

        editTextSPRatio = activity.findViewById(R.id.editTextSPRatio);
        editTextSPRatio.setText(String.valueOf(pref.getFloat("sp-ratio", 0.2F)));
        editTextSPRatio.setOnEditorActionListener((textView, i, keyEvent) -> {
            String rawText = String.valueOf(editTextSPRatio.getText());
            float spRatio;
            if (rawText.isEmpty()) {
                editTextSPRatio.setText(String.valueOf(pref.getFloat("sp-ratio", 0.2F)));
            } else {
                try {
                    spRatio = Float.parseFloat(rawText);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    editTextSPRatio.setText(String.valueOf(pref.getFloat("sp-ratio", 0.2F)));
                    editTextSPRatio.clearFocus();
                    return false;
                }
                if (spRatio > 1 || spRatio < 0) {
                    editTextSPRatio.setText(String.valueOf(pref.getFloat("sp-ratio", 0.2F)));
                } else {
                    synchronized (lock) {
                        editTextSPRatio.setText(String.valueOf(spRatio));
                        editor.putFloat("sp-ratio", spRatio);
                        editor.apply();
                    }
                }
            }
            editTextSPRatio.clearFocus();
            return false;
        });
    }
}