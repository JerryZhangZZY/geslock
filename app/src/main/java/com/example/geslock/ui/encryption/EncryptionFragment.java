package com.example.geslock.ui.encryption;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.fragment.app.Fragment;

import com.example.geslock.R;

public class EncryptionFragment extends Fragment {

    final int MAX_MOVE = 300;
    final int TAP_MOVE = 10;
    final float MAX_SCALE = 1.5F;
    final float SPIN_MOVE_RATIO = 0.2F;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_encryption, container, false);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final int[] initLayout = {0, 0, 0, 0};
        Vibrator vibrator = (Vibrator)getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);

        TextView testText = (TextView) getActivity().findViewById(R.id.testT);
        ImageView ball = (ImageView) getActivity().findViewById(R.id.ball);
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
            boolean triggered = false;
            int mode = 0;
            int fingerNum = 0;
            int startX1, startY1, moveX1, moveY1;
            int startX2, startY2, moveX2, moveY2;
            float angle;
            float startGap;
            int startCenterX, startCenterY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    // first finger contact
                    case MotionEvent.ACTION_DOWN:
                        startX1 = (int) motionEvent.getX() + ball.getLeft();
                        startY1 = (int) motionEvent.getY() + ball.getTop();
                        fingerNum = 1;
                        break;

                    // any finger moves
                    case MotionEvent.ACTION_MOVE:
                        moveX1 = (int) motionEvent.getX() + ball.getLeft();
                        moveY1 = (int) motionEvent.getY() + ball.getTop();
                        // single finger drag
                        if (fingerNum == 1) {
                            int deltaX = moveX1 - startX1;
                            int deltaY = moveY1 - startY1;
                            int left, top, right, bottom;
                            if (Math.abs(deltaX) - Math.abs(deltaY) > 0) {
                                // on X axis
                                top = initLayout[1];
                                bottom = initLayout[3];
                                if (Math.abs(moveX1 - startX1) < MAX_MOVE) {
                                    if (Math.abs(deltaX) >= TAP_MOVE) {
                                        left = initLayout[0] + deltaX;
                                        right = initLayout[2] + deltaX;
                                        ball.layout(left, top, right, bottom);
                                    }
                                } else {
                                    if (deltaX < 0) {
                                        if (!triggered) {
                                            triggered = true;

                                            // trigger single left !!!

                                            testText.setText("single left");
                                            vibrator.vibrate(20);
                                            left = initLayout[0] - MAX_MOVE;
                                            right = initLayout[2] - MAX_MOVE;
                                            ball.layout(left, top, right, bottom);
                                        }
                                    } else {
                                        if (!triggered) {
                                            triggered = true;

                                            // trigger single right !!!

                                            testText.setText("single right");
                                            vibrator.vibrate(20);
                                            left = initLayout[0] + MAX_MOVE;
                                            right = initLayout[2] + MAX_MOVE;
                                            ball.layout(left, top, right, bottom);
                                        }
                                    }
                                }
                            } else {
                                // on Y axis
                                left = initLayout[0];
                                right = initLayout[2];
                                if (Math.abs(moveY1 - startY1) < MAX_MOVE) {
                                    if (Math.abs(deltaY) >= TAP_MOVE) {
                                        top = initLayout[1] + deltaY;
                                        bottom = initLayout[3] + deltaY;
                                        ball.layout(left, top, right, bottom);
                                    }
                                } else {
                                    if (deltaY < 0) {
                                        if (!triggered) {
                                            triggered = true;

                                            // trigger single up !!!

                                            testText.setText("single up");
                                            vibrator.vibrate(20);
                                            top = initLayout[1] - MAX_MOVE;
                                            bottom = initLayout[3] - MAX_MOVE;
                                            ball.layout(left, top, right, bottom);
                                        }
                                    } else {
                                        if (!triggered) {
                                            triggered = true;

                                            // trigger single down !!!

                                            testText.setText("single down");
                                            vibrator.vibrate(20);
                                            top = initLayout[1] + MAX_MOVE;
                                            bottom = initLayout[3] + MAX_MOVE;
                                            ball.layout(left, top, right, bottom);
                                        }
                                    }
                                }
                            }
                        } else if (motionEvent.getPointerCount() == 2) {
                            // still possible to have only one finger
                            // catch exception to make sure the app wouldn't crash
                            try {
                                moveX2 = (int) motionEvent.getX(1) + ball.getLeft();
                                moveY2 = (int) motionEvent.getY(1) + ball.getTop();
                                int[] moveXY = transformCor(new int[]{moveX1, moveY1}, (int) ball.getRotation(), ball.getScaleX());
                                moveX1 = moveXY[0];
                                moveY1 = moveXY[1];
                                moveXY = transformCor(new int[]{moveX2, moveY2}, (int) ball.getRotation(), ball.getScaleX());
                                moveX2 = moveXY[0];
                                moveY2 = moveXY[1];

                                float moveGap = dist(moveX1, moveY1, moveX2, moveY2);
                                float deltaGap = moveGap - startGap;

                                int moveCenterX = (moveX1 + moveX2) / 2;
                                int moveCenterY = (moveY1 + moveY2) / 2;
                                float deltaCenter = dist(startCenterX, startCenterY, moveCenterX, moveCenterY);

                                switch (mode) {
                                    // unjudged
                                    case 0:
                                        if (Math.abs(deltaGap) > 20) {
                                            if (deltaCenter < 100) {
                                                // judge as zoom in/out
                                                mode = 1;
                                            }
                                        } else if (deltaCenter > 20)
                                            // judge as double fingers swipe
                                            mode = 2;
                                        break;
                                    // zoom in/out
                                    case 1:
                                        float rotation = (float) Math.toDegrees(Math.atan2(moveY2 - moveY1, moveX2 - moveX1)) - angle;
                                        if (rotation > 180)
                                            rotation = rotation - 360;
                                        if (rotation < -180)
                                            rotation = rotation + 360;
                                        ball.setRotation(rotation);
                                        float scale = moveGap / startGap;
                                        if (scale >= MAX_SCALE) {
                                            if (!triggered) {
                                                triggered = true;

                                                // trigger zoom in !!!

                                                testText.setText("zoom in");
                                                vibrator.vibrate(20);
                                                ball.setScaleX(MAX_SCALE);
                                                ball.setScaleY(MAX_SCALE);
                                            }
                                        } else if (scale <= 1 / MAX_SCALE) {
                                            if (!triggered) {
                                                triggered = true;

                                                // trigger zoom out !!!

                                                testText.setText("zoom out");
                                                vibrator.vibrate(20);
                                                ball.setScaleX(1 / MAX_SCALE);
                                                ball.setScaleY(1 / MAX_SCALE);
                                            }
                                        } else {
                                            ball.setScaleX(scale);
                                            ball.setScaleY(scale);
                                        }
                                        break;
                                    // double fingers swipe
                                    case 2:
                                        int deltaCenterX = moveCenterX - startCenterX;
                                        int deltaCenterY = moveCenterY - startCenterY;
                                        int spinLeft, spinTop, spinRight, spinBottom;
                                        float spinMaxMove = MAX_MOVE * SPIN_MOVE_RATIO;
                                        if (Math.abs(deltaCenterX) > Math.abs(deltaCenterY)) {
                                            // on X axis
                                            spinTop = initLayout[1];
                                            spinBottom = initLayout[3];
                                            if (Math.abs(deltaCenterX) < MAX_MOVE) {
                                                if (Math.abs(deltaCenterX) >= TAP_MOVE) {
                                                    spinLeft = (int) (initLayout[0] + (deltaCenterX * SPIN_MOVE_RATIO));
                                                    spinRight = (int) (initLayout[2] + (deltaCenterX * SPIN_MOVE_RATIO));
                                                    ball.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                }
                                            } else {
                                                if (deltaCenterX < 0) {
                                                    if (!triggered) {
                                                        triggered = true;

                                                        // trigger double left !!!

                                                        testText.setText("double left");
                                                        vibrator.vibrate(20);
                                                        spinLeft = (int) (initLayout[0] - spinMaxMove);
                                                        spinRight = (int) (initLayout[2] - spinMaxMove);
                                                        ball.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                    }
                                                } else {
                                                    if (!triggered) {
                                                        triggered = true;

                                                        // trigger double left !!!

                                                        testText.setText("double right");
                                                        vibrator.vibrate(20);
                                                        spinLeft = (int) (initLayout[0] + spinMaxMove);
                                                        spinRight = (int) (initLayout[2] + spinMaxMove);
                                                        ball.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                    }
                                                }
                                            }

                                            ball.setImageResource(R.drawable.ic_soccer_spin_x);
                                            ball.setTag("spin_x");
                                        } else {
                                            // on Y axis
                                            spinLeft = initLayout[0];
                                            spinRight = initLayout[2];
                                            if (Math.abs(deltaCenterY) < MAX_MOVE) {
                                                if (Math.abs(deltaCenterY) >= TAP_MOVE) {
                                                    spinTop = (int) (initLayout[1] + (deltaCenterY * SPIN_MOVE_RATIO));
                                                    spinBottom = (int) (initLayout[3] + (deltaCenterY * SPIN_MOVE_RATIO));
                                                    ball.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                }
                                            } else {
                                                if (deltaCenterY < 0) {
                                                    if (!triggered) {
                                                        triggered = true;

                                                        // trigger double up !!!

                                                        testText.setText("double up");
                                                        vibrator.vibrate(20);
                                                        spinTop = (int) (initLayout[1] - spinMaxMove);
                                                        spinBottom = (int) (initLayout[3] - spinMaxMove);
                                                        ball.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                    }
                                                } else {
                                                    if (!triggered) {
                                                        triggered = true;

                                                        // trigger double down !!!

                                                        testText.setText("double down");
                                                        vibrator.vibrate(20);
                                                        spinTop = (int) (initLayout[1] + spinMaxMove);
                                                        spinBottom = (int) (initLayout[3] + spinMaxMove);
                                                        ball.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                    }
                                                }
                                            }
                                            ball.setImageResource(R.drawable.ic_soccer_spin_y);
                                            ball.setTag("spin_y");
                                        }
                                        break;
                                }
                            } catch (IllegalArgumentException ignored) {}
                        }
                        break;

                    // second finger contact
                    case MotionEvent.ACTION_POINTER_DOWN:
                        startX2 = (int) motionEvent.getX(1) + ball.getLeft();
                        startY2 = (int) motionEvent.getY(1) + ball.getTop();
                        fingerNum = 2;
                        ball.layout(initLayout[0], initLayout[1], initLayout[2], initLayout[3]);
                        startGap = dist(startX1, startY1, startX2, startY2);
                        startCenterX = (startX1 + startX2) / 2;
                        startCenterY = (startY1 + startY2) / 2;
                        angle = (float) Math.toDegrees(Math.atan2(startY2 - startY1, startX2 - startX1));
                        break;

//                    case MotionEvent.ACTION_POINTER_UP:
//                        fingerNum = 1;
//                        break;

                    // all fingers leave
                    case MotionEvent.ACTION_UP:
                        // single finger tap
                        if (fingerNum == 1 && dist(startX1, startY1, (int) motionEvent.getX() + ball.getLeft(), (int) motionEvent.getY() + ball.getTop()) < TAP_MOVE) {

                            // trigger single tap !!!

                            ScaleAnimation ta = new ScaleAnimation(1, 1.1F, 1, 1.1F, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                            ta.setDuration(50);
                            ball.startAnimation(ta);
                            testText.setText("single tap");
                            vibrator.vibrate(20);
                        }

                        // reset params
                        fingerNum = 0;
                        triggered = false;
                        mode = 0;

                        // ball reset animations
                        if (ball.getLeft() != initLayout[0] || ball.getTop() != initLayout[1]) {
                            TranslateAnimation ta = new TranslateAnimation(0, initLayout[0] - ball.getLeft(), 0, initLayout[1] - ball.getTop());
                            ta.setDuration(100);
                            ta.setInterpolator(new OvershootInterpolator(3));
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
                        }
                        if (ball.getRotation() != 0 || ball.getScaleX() != 1) {
                            RotateAnimation ra = new RotateAnimation(0, -ball.getRotation(), Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            ra.setDuration(100);
                            ra.setInterpolator(new OvershootInterpolator(3));
                            ra.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {}
                                @Override
                                public void onAnimationEnd(Animation animation) { ball.clearAnimation(); }
                                @Override
                                public void onAnimationRepeat(Animation animation) {}
                            });

                            ScaleAnimation sa = new ScaleAnimation(1, 1 / ball.getScaleX(), 1, 1 / ball.getScaleY(), Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                            sa.setDuration(100);
                            sa.setInterpolator(new OvershootInterpolator(3));
                            sa.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {}
                                @Override
                                public void onAnimationEnd(Animation animation) { ball.clearAnimation(); }
                                @Override
                                public void onAnimationRepeat(Animation animation) {}
                            });

                            AnimationSet as = new AnimationSet(false);
                            as.addAnimation(ra);
                            as.addAnimation(sa);
                            as.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {}
                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    ball.setRotation(0);
                                    ball.setScaleX(1);
                                    ball.setScaleY(1);
                                }
                                @Override
                                public void onAnimationRepeat(Animation animation) {}
                            });
                            ball.startAnimation(as);
                        }
                        if (!ball.getTag().equals("origin")) {
                            ball.setImageResource(R.drawable.ic_soccer);
                            ball.setTag("origin");
                        }
                        break;
                }
                return true;
            }
        });
    }

    public float dist(int x1, int y1, int x2, int y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    private int[] transformCor(int[] xy, int rotation, float scale) {
        int[] result = new int[2];
        int x1 = (int) (xy[0] * scale);
        int y1 = (int) (xy[1] * scale);
        result[0] = (int) ((x1) * Math.cos(Math.PI / 180.0 * rotation) - (y1) * Math.sin(Math.PI / 180.0 * rotation));
        result[1] = (int) ((x1) * Math.sin(Math.PI / 180.0 * rotation) + (y1) * Math.cos(Math.PI / 180.0 * rotation));
        return result;
    }
}