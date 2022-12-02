package com.example.geslock.ui.settings;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
        Vibrator vibrator = (Vibrator)getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);

        ImageButton[] dragIcons = new ImageButton[3];
        dragIcons[0] = (ImageButton) getActivity().findViewById(R.id.imgBtnSoccer);
        dragIcons[1] = (ImageButton) getActivity().findViewById(R.id.imgBtnFace);
        dragIcons[2] = (ImageButton) getActivity().findViewById(R.id.imgBtnBasketball);

        Drawable border = getResources().getDrawable(R.drawable.round_btn_border);

        for (ImageButton imageButton : dragIcons) {
            imageButton.setBackground(null);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (ImageButton imageButton : dragIcons)
                        imageButton.setBackground(null);

                    ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(border, PropertyValuesHolder.ofInt("alpha", 0, 255));
                    animator.setTarget(border);
                    animator.setDuration(200);
                    animator.start();
                    imageButton.setBackground(border);
                }
            });

        }




//        dragIcons[0].setBackground();


    }
}