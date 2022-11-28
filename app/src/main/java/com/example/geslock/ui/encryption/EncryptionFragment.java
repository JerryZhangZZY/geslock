package com.example.geslock.ui.encryption;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
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
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final int[] initLayout = {0, 0, 0, 0, 0};
        Vibrator vibrator = (Vibrator)getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);

        TextView testText = (TextView) getActivity().findViewById(R.id.testT);

        ImageView ball = (ImageView) getActivity().findViewById(R.id.basketball);
        ball.post(new Runnable() {
            @Override
            public void run() {
                initLayout[0] = ball.getLeft();
                initLayout[1] = ball.getTop();
                initLayout[2] = ball.getRight();
                initLayout[3] = ball.getBottom();
            }
        });
        ball.setOnTouchListener(new View.OnTouchListener() {
            final int MAX_MOVE = 300;
            final int TAP_MOVE = 10;

            boolean triggered = false;
            int fingerNum = 0;
            int startX, startY, moveX, moveY;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    // first finger contact
                    case MotionEvent.ACTION_DOWN:
                        fingerNum = 1;
                        startX = (int)motionEvent.getRawX();
                        startY = (int)motionEvent.getRawY();
                        break;

                    // any finger moves
                    case MotionEvent.ACTION_MOVE:
                        // single finger drag
                        if (fingerNum == 1) {
                            moveX = (int) motionEvent.getRawX();
                            moveY = (int) motionEvent.getRawY();
                            int deltaX = moveX - startX;
                            int deltaY = moveY - startY;
                            int left = initLayout[0] + deltaX;
                            int top = initLayout[1] + deltaY;
                            if (Math.abs(deltaX) - Math.abs(deltaY) > 0) {
                                // on X axis
                                if (Math.abs(left - initLayout[0]) < MAX_MOVE) {
                                    int right = left + ball.getWidth();
                                    ball.layout(left, initLayout[1], right, initLayout[3]);
                                }
                                else {
                                    if (left - initLayout[0] < 0) {
                                        if (!triggered) {
                                            triggered = true;
                                            // trigger single left !!!
                                            testText.setText("single left");
                                            vibrator.vibrate(20);
                                            ball.layout(initLayout[0] - MAX_MOVE, initLayout[1], initLayout[2] - MAX_MOVE, initLayout[3]);
                                        }
                                    } else {
                                        if (!triggered) {
                                            triggered = true;
                                            // trigger single right !!!
                                            testText.setText("single right");
                                            vibrator.vibrate(20);
                                            ball.layout(initLayout[0] + MAX_MOVE, initLayout[1], initLayout[2] + MAX_MOVE, initLayout[3]);
                                        }
                                    }
                                }
                            } else {
                                // on Y axis
                                if (Math.abs(top - initLayout[1]) < MAX_MOVE) {
                                    int bottom = top + ball.getHeight();
                                    ball.layout(initLayout[0], top, initLayout[2], bottom);
                                }
                                else {
                                    if (top - initLayout[1] < 0) {
                                        if (!triggered) {
                                            triggered = true;

                                            // trigger single up !!!

                                            testText.setText("single up");
                                            vibrator.vibrate(20);
                                            ball.layout(initLayout[0], initLayout[1] - MAX_MOVE, initLayout[2], initLayout[3] - MAX_MOVE);
                                        }
                                    } else {
                                        if (!triggered) {
                                            triggered = true;

                                            // trigger single down !!!

                                            testText.setText("single down");
                                            vibrator.vibrate(20);
                                            ball.layout(initLayout[0], initLayout[1] + MAX_MOVE, initLayout[2], initLayout[3] + MAX_MOVE);
                                        }
                                    }
                                }
                            }
                        } else if (fingerNum == 2) {

                        }
                        break;

                    // second finger contact
                    case MotionEvent.ACTION_POINTER_DOWN:
                        fingerNum = motionEvent.getPointerCount();
                        ball.layout(initLayout[0], initLayout[1], initLayout[2], initLayout[3]);
                        break;

                    // all fingers leave
                    case MotionEvent.ACTION_UP:
                        // reset params
                        fingerNum = 0;
                        triggered = false;

                        // single finger tap
                        if (dist(startX, startY, (int)motionEvent.getRawX(), (int)motionEvent.getRawY()) < TAP_MOVE) {

                            // trigger single tap !!!

                            testText.setText("single tap");
                            vibrator.vibrate(20);
                        }

                        // ball reset animations
                        TranslateAnimation ta = new TranslateAnimation(0, initLayout[0] - ball.getLeft(), 0, initLayout[1] - ball.getTop());
                        ta.setDuration(50);
                        ta.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                ball.clearAnimation();
                                ball.layout(initLayout[0], initLayout[1], initLayout[2], initLayout[3]);
                            }
                            @Override
                            public void onAnimationStart(Animation animation) {}
                            @Override
                            public void onAnimationRepeat(Animation animation) {}
                        });
                        ball.startAnimation(ta);
                        break;
                }
                return true;
            }
        });
    }

    public double dist(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
}