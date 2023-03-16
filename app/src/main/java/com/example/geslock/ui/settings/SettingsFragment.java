package com.example.geslock.ui.settings;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.geslock.BuildConfig;
import com.example.geslock.R;
import com.example.geslock.tools.MyAnimationScaler;
import com.example.geslock.tools.MyDefaultPref;
import com.example.geslock.tools.MyToastMaker;
import com.example.geslock.tools.MyVibrator;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private Activity activity;
    private SharedPreferences pref;

    private ImageButton[] rockerIcons;
    private Drawable border;
    private Switch switchCross;
    private Switch switchItemCount;
    private Spinner spinnerTheme;
    private Spinner spinnerLanguage;
    private TextView tvAbout;
    private TextView tvUpdate;
    private Spinner spinnerTravel;
    private Switch switchVibration;
    private Spinner spinnerAnimation;
    private TextView tvOvershoot;
    private EditText editTextOvershoot;
    private TextView tvSMRatio;
    private EditText editTextSMRatio;
    private TextView tvTMRatio;
    private EditText editTextTMRatio;
    private TextView tvReset;

    private final Object lock = new Object();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        assert activity != null;
        pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        rockerIcons = new ImageButton[3];
        rockerIcons[0] = activity.findViewById(R.id.imgBtnSoccer);
        rockerIcons[1] = activity.findViewById(R.id.imgBtnDonut);
        rockerIcons[2] = activity.findViewById(R.id.imgBtnBasketball);

        border = ResourcesCompat.getDrawable(getResources(), R.drawable.round_btn_border, null);

        for (int index = 0; index < rockerIcons.length; index++) {
            rockerIcons[index].setBackground(pref.getInt("icon", MyDefaultPref.getDefaultInt("icon")) == index ? border : null);
            int finalIndex = index;
            rockerIcons[index].setOnClickListener(view -> {
                synchronized (lock) {
                    MyVibrator.tick(requireActivity());
                    for (ImageButton btn : rockerIcons)
                        btn.setBackground(null);
                    ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(border, PropertyValuesHolder.ofInt("alpha", 0, 255));
                    animator.setTarget(border);
                    animator.setDuration(MyAnimationScaler.getDuration(100, activity));
                    animator.start();
                    rockerIcons[finalIndex].setBackground(border);
                    editor.putInt("icon", finalIndex);
                    editor.apply();
                }
            });
        }

        switchCross = activity.findViewById(R.id.switchCross);
        switchCross.setChecked(pref.getBoolean("cross", MyDefaultPref.getDefaultBoolean("cross")));
        switchCross.setOnCheckedChangeListener((compoundButton, b) -> {
            editor.putBoolean("cross", b);
            editor.apply();
        });

        switchItemCount = activity.findViewById(R.id.switchItemCount);
        switchItemCount.setChecked(pref.getBoolean("item-count", MyDefaultPref.getDefaultBoolean("item-count")));
        switchItemCount.setOnCheckedChangeListener((compoundButton, b) -> {
            editor.putBoolean("item-count", b);
            editor.apply();
        });

        spinnerTheme = activity.findViewById(R.id.spinnerTheme);
        switch (pref.getInt("theme", MyDefaultPref.getDefaultInt("theme"))) {
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
                            synchronized (lock) {
                                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                                editor.putInt("theme", MODE_NIGHT_NO);
                                editor.apply();
                            }
                            break;
                        case 1:
                            synchronized (lock) {
                                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                                editor.putInt("theme", MODE_NIGHT_YES);
                                editor.apply();
                            }
                            break;
                        case 2:
                            synchronized (lock) {
                                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                                editor.putInt("theme", MODE_NIGHT_FOLLOW_SYSTEM);
                                editor.apply();
                            }
                            break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinnerLanguage = activity.findViewById(R.id.spinnerLanguage);
        spinnerLanguage.setSelection(pref.getInt("language", MyDefaultPref.getDefaultInt("language")));
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        final boolean[] enabledFlag = {false};
        spinnerLanguage.setOnTouchListener((view, motionEvent) -> {
            enabledFlag[0] = true;
            return false;
        });
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (enabledFlag[0]) {
                    synchronized (lock) {
                        switch (i) {
                            case 0:
                                configuration.setLocale(Locale.ENGLISH);
                                break;
                            case 1:
                                configuration.setLocale(Locale.CHINESE);
                                break;
                            case 2:
                                configuration.setLocale(Locale.getDefault());
                                break;
                        }
                        resources.updateConfiguration(configuration, displayMetrics);
                        editor.putInt("language", i);
                        editor.apply();
                        activity.recreate();
                    }
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        tvAbout = activity.findViewById(R.id.tvAbout);
        tvAbout.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.app_name))
                    .setMessage(activity.getString(R.string.about_message))
                    .setNegativeButton(R.string.cancel, (dialog0, which) -> dialog0.dismiss())
                    .setPositiveButton(R.string.about_repo, (dialog0, which) -> {
                        String url = "https://github.com/JerryZhangZZY/geslock";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            MyToastMaker.make(activity.getString(R.string.error), activity);
                        }
                        dialog0.dismiss();
                    }).create();
            try {
                dialog.setIcon(activity.getPackageManager().getApplicationIcon("com.example.geslock"));
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);
            dialog.show();
            int yellow_500 = activity.getColor(R.color.yellow_500);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(yellow_500);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(yellow_500);
        });

        tvUpdate = activity.findViewById(R.id.tvUpdate);
        String strUpdate = activity.getString(R.string.settings_entry_update);
        String strVersion = activity.getString(R.string.settings_entry_current) + BuildConfig.VERSION_NAME;
        int strUpdateLength = strUpdate.length();
        TypedValue colorMainValue = new TypedValue();
        TypedValue colorSubValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.textColorPrimary, colorMainValue, true);
        activity.getTheme().resolveAttribute(android.R.attr.subtitleTextColor, colorSubValue, true);
        SpannableString strUpdateAndVersion = new SpannableString(strUpdate + " (" + strVersion + ")");
        ForegroundColorSpan colorMain = new ForegroundColorSpan(colorMainValue.data);
        ForegroundColorSpan colorSub = new ForegroundColorSpan(colorSubValue.data);
        strUpdateAndVersion.setSpan(colorMain, 0, strUpdateLength - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        strUpdateAndVersion.setSpan(colorSub, strUpdateLength, strUpdateAndVersion.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        strUpdateAndVersion.setSpan(new AbsoluteSizeSpan(12, true), strUpdateLength, strUpdateAndVersion.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvUpdate.setText(strUpdateAndVersion);
        tvUpdate.setOnClickListener(v -> {
            String url = "https://github.com/JerryZhangZZY/geslock/releases";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                startActivity(intent);
            } catch (Exception e) {
                MyToastMaker.make(activity.getString(R.string.error), activity);
            }
        });

        spinnerTravel = activity.findViewById(R.id.spinnerTravel);
        spinnerTravel.setSelection(pref.getInt("travel", MyDefaultPref.getDefaultInt("travel")));
        spinnerTravel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                editor.putInt("travel", i);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        switchVibration = activity.findViewById(R.id.switchVibration);
        switchVibration.setChecked(pref.getBoolean("vibration", MyDefaultPref.getDefaultBoolean("vibration")));
        switchVibration.setOnCheckedChangeListener((compoundButton, b) -> {
            editor.putBoolean("vibration", b);
            editor.apply();
        });

        spinnerAnimation = activity.findViewById(R.id.spinnerAnimation);
        spinnerAnimation.setSelection(pref.getInt("animation", MyDefaultPref.getDefaultInt("animation")));
        spinnerAnimation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                editor.putInt("animation", i);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        tvOvershoot = activity.findViewById(R.id.tvOvershoot);
        tvOvershoot.setOnClickListener(view -> MyToastMaker.make(String.valueOf(activity.getText(R.string.overshoot_description)), activity));

        editTextOvershoot = activity.findViewById(R.id.editTextOvershoot);
        editTextOvershoot.setText(String.valueOf(pref.getFloat("overshoot", MyDefaultPref.getDefaultFloat("overshoot"))));
        editTextOvershoot.setOnFocusChangeListener((view, b) -> {
            String rawText = String.valueOf(editTextOvershoot.getText());
            float overshoot;
            // empty input
            if (rawText.isEmpty()) {
                editTextOvershoot.setText(String.valueOf(pref.getFloat("overshoot", MyDefaultPref.getDefaultFloat("overshoot"))));
            } else {
                try {
                    overshoot = Float.parseFloat(rawText);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    editTextOvershoot.setText(String.valueOf(pref.getFloat("overshoot", MyDefaultPref.getDefaultFloat("overshoot"))));
                    return;
                }
                // input out of bounds
                if (overshoot > 1 || overshoot < 0) {
                    editTextOvershoot.setText(String.valueOf(pref.getFloat("overshoot", MyDefaultPref.getDefaultFloat("overshoot"))));
                } else {
                    synchronized (lock) {
                        editTextOvershoot.setText(String.valueOf(overshoot));
                        editor.putFloat("overshoot", overshoot);
                        editor.apply();
                    }
                }
            }
        });
        editTextOvershoot.setOnEditorActionListener((textView, i, keyEvent) -> {
            editTextOvershoot.clearFocus();
            return false;
        });

        tvSMRatio = activity.findViewById(R.id.tvSMRatio);
        tvSMRatio.setOnClickListener(view -> MyToastMaker.make(String.valueOf(activity.getText(R.string.smratio_description)), activity));

        editTextSMRatio = activity.findViewById(R.id.editTextSMRatio);
        editTextSMRatio.setText(String.valueOf(pref.getFloat("sm-ratio", MyDefaultPref.getDefaultFloat("sm-ratio"))));
        editTextSMRatio.setOnFocusChangeListener((view, b) -> {
            String rawText = String.valueOf(editTextSMRatio.getText());
            float smRatio;
            // empty input
            if (rawText.isEmpty()) {
                editTextSMRatio.setText(String.valueOf(pref.getFloat("sm-ratio", MyDefaultPref.getDefaultFloat("sm-ratio"))));
            } else {
                try {
                    smRatio = Float.parseFloat(rawText);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    editTextSMRatio.setText(String.valueOf(pref.getFloat("sm-ratio", MyDefaultPref.getDefaultFloat("sm-ratio"))));
                    return;
                }
                // input out of bounds
                if (smRatio > 1 || smRatio < 0) {
                    editTextSMRatio.setText(String.valueOf(pref.getFloat("sm-ratio", MyDefaultPref.getDefaultFloat("sm-ratio"))));
                } else {
                    synchronized (lock) {
                        editTextSMRatio.setText(String.valueOf(smRatio));
                        editor.putFloat("sm-ratio", smRatio);
                        editor.apply();
                    }
                }
            }
        });
        editTextSMRatio.setOnEditorActionListener((textView, i, keyEvent) -> {
            editTextSMRatio.clearFocus();
            return false;
        });

        tvTMRatio = activity.findViewById(R.id.tvTMRatio);
        tvTMRatio.setOnClickListener(view -> MyToastMaker.make(String.valueOf(activity.getText(R.string.tmratio_description)), activity));

        editTextTMRatio = activity.findViewById(R.id.editTextTMRatio);
        editTextTMRatio.setText(String.valueOf(pref.getFloat("tm-ratio", MyDefaultPref.getDefaultFloat("tm-ratio"))));
        editTextTMRatio.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                String rawText = String.valueOf(editTextTMRatio.getText());
                float tmRatio;
                // empty input
                if (rawText.isEmpty()) {
                    editTextTMRatio.setText(String.valueOf(pref.getFloat("tm-ratio", MyDefaultPref.getDefaultFloat("tm-ratio"))));
                } else {
                    try {
                        tmRatio = Float.parseFloat(rawText);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        editTextTMRatio.setText(String.valueOf(pref.getFloat("tm-ratio", MyDefaultPref.getDefaultFloat("tm-ratio"))));
                        return;
                    }
                    // input out of bounds
                    if (tmRatio > 0.1 || tmRatio <= 0) {
                        editTextTMRatio.setText(String.valueOf(pref.getFloat("tm-ratio", MyDefaultPref.getDefaultFloat("tm-ratio"))));
                    } else {
                        synchronized (lock) {
                            editTextTMRatio.setText(String.valueOf(tmRatio));
                            editor.putFloat("tm-ratio", tmRatio);
                            editor.apply();
                        }
                    }
                }
            }
        });
        editTextTMRatio.setOnEditorActionListener((textView, i, keyEvent) -> {
            editTextTMRatio.clearFocus();
            return false;
        });

        tvReset = activity.findViewById(R.id.tvReset);
        tvReset.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.reset_title))
                    .setNegativeButton(R.string.cancel, (dialog0, which) -> dialog0.dismiss())
                    .setPositiveButton(R.string.ok, (dialog0, which) -> {
                        MyDefaultPref.resetToDefault(editor);
                        triggerRebirth(activity);
                        dialog0.dismiss();
                    }).create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(activity.getColor(R.color.red_500));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(activity.getColor(R.color.yellow_500));
        });
    }

    /**
     * Rebirth the app to refresh all settings.
     *
     * @param context context
     */
    public void triggerRebirth(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }
}