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
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.geslock.R;
import com.example.geslock.tool.MyVibrator;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();

        SharedPreferences pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        ImageButton[] dragIcons = new ImageButton[3];
        dragIcons[0] = activity.findViewById(R.id.imgBtnSoccer);
        dragIcons[1] = activity.findViewById(R.id.imgBtnDonut);
        dragIcons[2] = activity.findViewById(R.id.imgBtnBasketball);

        Drawable border = getResources().getDrawable(R.drawable.round_btn_border);

        for (int index = 0; index < dragIcons.length; index++) {
            dragIcons[index].setBackground(pref.getInt("icon", 0) == index ? border : null);
            int finalIndex = index;
            dragIcons[index].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MyVibrator.tick(requireActivity());
                    for (ImageButton btn : dragIcons)
                        btn.setBackground(null);
                    ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(border, PropertyValuesHolder.ofInt("alpha", 0, 255));
                    animator.setTarget(border);
                    animator.setDuration(200);
                    animator.start();
                    dragIcons[finalIndex].setBackground(border);
                    editor.putInt("icon", finalIndex);
                    editor.apply();
                }
            });
        }

        Spinner theme = activity.findViewById(R.id.spinnerTheme);
        switch (pref.getInt("theme", MODE_NIGHT_FOLLOW_SYSTEM)) {
            case MODE_NIGHT_NO:
                theme.setSelection(0);
                break;
            case MODE_NIGHT_YES:
                theme.setSelection(1);
                break;
            case MODE_NIGHT_FOLLOW_SYSTEM:
                theme.setSelection(2);
                break;
        }
        theme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
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
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        Spinner language = activity.findViewById(R.id.spinnerLanguage);
        language.setSelection(pref.getInt("language", 2));
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        final boolean[] enabledFlag = {false};
        language.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                enabledFlag[0] = true;
                return false;
            }
        });
        language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (enabledFlag[0]) {
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
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        Spinner dragDist = activity.findViewById(R.id.spinnerDragDist);
        dragDist.setSelection(pref.getInt("drag-dist", 1));
        dragDist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        Switch switchVibration = activity.findViewById(R.id.switchVibration);
        switchVibration.setChecked(pref.getBoolean("vibration", true));
        switchVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean("vibration", b);
                editor.apply();
            }
        });
    }
}