package com.example.geslock.ui.settings;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.geslock.R;
import com.example.geslock.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        Vibrator vibrator = (Vibrator)getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);

        ImageButton[] dragIcons = new ImageButton[3];
        dragIcons[0] = getActivity().findViewById(R.id.imgBtnSoccer);
        dragIcons[1] = getActivity().findViewById(R.id.imgBtnDonut);
        dragIcons[2] = getActivity().findViewById(R.id.imgBtnBasketball);

        Drawable border = getResources().getDrawable(R.drawable.round_btn_border);

        for (int index = 0; index < dragIcons.length; index++) {
            dragIcons[index].setBackground(pref.getInt("icon", 0) == index ? border : null);
            int finalIndex = index;
            dragIcons[index].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tick(vibrator);
                    for (ImageButton btn : dragIcons)
                        btn.setBackground(null);
                    ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(border, PropertyValuesHolder.ofInt("alpha", 0, 255));
                    animator.setTarget(border);
                    animator.setDuration(200);
                    animator.start();
                    dragIcons[finalIndex].setBackground(border);
                    editor.putInt("icon", finalIndex);
                    editor.apply();

                    if (finalIndex == 0)
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                    else if (finalIndex == 1)
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                    else
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                }
            });
        }
    }

    private void tick(Vibrator vibrator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.EFFECT_TICK);
        } else {
            vibrator.vibrate(20);
        }
    }
}