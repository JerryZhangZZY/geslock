package com.example.geslock.ui.encryption;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.geslock.R;

public class EncryptionFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_encryption, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImageView ball = (ImageView) getActivity().findViewById(R.id.basketball);
        ball.setOnTouchListener(new View.OnTouchListener() {
            private int startX, startY;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int)motionEvent.getRawX();
                        startY = (int)motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int moveX = (int) motionEvent.getRawX();
                        int moveY = (int) motionEvent.getRawY();
                        int deltaX = moveX - startX;
                        int deltaY = moveY - startY;
                        int left = ball.getLeft();
                        int top = ball.getTop();
                        left += deltaX;
                        top += deltaY;
                        int right = left + ball.getWidth();
                        int bottom = top + ball.getHeight();
                        ball.layout(left, top, right, bottom);
                        startX = moveX;
                        startY = moveY;
                        break;
                }
                return true;
            }
        });
    }
}