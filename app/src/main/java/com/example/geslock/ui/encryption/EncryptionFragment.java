package com.example.geslock.ui.encryption;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.geslock.R;
import com.example.geslock.tools.MyAnimationScaler;
import com.example.geslock.tools.MyVibrator;

public class EncryptionFragment extends Fragment {

    private int MAX_MOVE = 300;
    private float MAX_SCALE = 1.5F;
    private int ICON_INDEX = 0;
    private float SPIN_MOVE_RATIO = 0.2F;
    private float DOUBLE_JUDGE_MOVE = 43.2F;

    private final int TAP_MOVE = 10;

    private final int[] fragmentSize = new int[2];
    private final int[][] rockerIcons = new int[3][3];
    private final int[] rockerInitLayout = new int[4];
    private TextView testText;
    private ImageView rocker;
    private ImageView cross;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_encryption, container, false);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set activity
        Activity activity = getActivity();
        // get preferences
        assert activity != null;
        SharedPreferences pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE);

        // set icon selection
        ICON_INDEX = pref.getInt("icon", 0);

        requireView().post(() -> {
            // set fragment measurements
            fragmentSize[0] = requireView().getWidth();
            fragmentSize[1] = requireView().getHeight();
            // set drag params
            setRockerParams(fragmentSize, pref);
        });

        // set all icons
        rockerIcons[0][0] = R.drawable.ic_soccer;
        rockerIcons[0][1] = R.drawable.ic_soccer_spin_x;
        rockerIcons[0][2] = R.drawable.ic_soccer_spin_y;
        rockerIcons[1][0] = R.drawable.ic_donut;
        rockerIcons[1][1] = R.drawable.ic_donut_spin_x;
        rockerIcons[1][2] = R.drawable.ic_donut_spin_y;
        rockerIcons[2][0] = R.drawable.ic_basketball;
        rockerIcons[2][1] = R.drawable.ic_basketball_spin_x;
        rockerIcons[2][2] = R.drawable.ic_basketball_spin_y;

        // get widgets
        testText = activity.findViewById(R.id.testT);
        rocker = activity.findViewById(R.id.rocker);
        cross = activity.findViewById(R.id.cross);

        cross.setVisibility(pref.getBoolean("cross", true) ? View.VISIBLE : View.INVISIBLE);

        rocker.setImageResource(rockerIcons[ICON_INDEX][0]);
        rocker.post(() -> {
            rockerInitLayout[0] = rocker.getLeft();
            rockerInitLayout[1] = rocker.getTop();
            rockerInitLayout[2] = rocker.getRight();
            rockerInitLayout[3] = rocker.getBottom();
        });
        rocker.setOnTouchListener(new View.OnTouchListener() {
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
                        startX1 = (int) motionEvent.getX() + rocker.getLeft();
                        startY1 = (int) motionEvent.getY() + rocker.getTop();
                        fingerNum = 1;
                        break;

                    // any finger moves
                    case MotionEvent.ACTION_MOVE:
                        moveX1 = (int) motionEvent.getX() + rocker.getLeft();
                        moveY1 = (int) motionEvent.getY() + rocker.getTop();
                        // single finger drag
                        if (fingerNum == 1) {
                            int deltaX = moveX1 - startX1;
                            int deltaY = moveY1 - startY1;
                            int left, top, right, bottom;
                            if (Math.abs(deltaX) - Math.abs(deltaY) > 0) {
                                // on X axis
                                top = rockerInitLayout[1];
                                bottom = rockerInitLayout[3];
                                if (Math.abs(moveX1 - startX1) < MAX_MOVE) {
                                    if (Math.abs(deltaX) >= TAP_MOVE) {
                                        left = rockerInitLayout[0] + deltaX;
                                        right = rockerInitLayout[2] + deltaX;
                                        rocker.layout(left, top, right, bottom);
                                    }
                                } else {
                                    if (deltaX < 0) {
                                        if (!triggered) {
                                            triggered = true;

                                            // trigger single left !!!

                                            testText.setText("single left");
                                            MyVibrator.tick(requireActivity());
                                            left = rockerInitLayout[0] - MAX_MOVE;
                                            right = rockerInitLayout[2] - MAX_MOVE;
                                            rocker.layout(left, top, right, bottom);
                                        }
                                    } else {
                                        if (!triggered) {
                                            triggered = true;

                                            // trigger single right !!!

                                            testText.setText("single right");
                                            MyVibrator.tick(requireActivity());
                                            left = rockerInitLayout[0] + MAX_MOVE;
                                            right = rockerInitLayout[2] + MAX_MOVE;
                                            rocker.layout(left, top, right, bottom);
                                        }
                                    }
                                }
                            } else {
                                // on Y axis
                                left = rockerInitLayout[0];
                                right = rockerInitLayout[2];
                                if (Math.abs(moveY1 - startY1) < MAX_MOVE) {
                                    if (Math.abs(deltaY) >= TAP_MOVE) {
                                        top = rockerInitLayout[1] + deltaY;
                                        bottom = rockerInitLayout[3] + deltaY;
                                        rocker.layout(left, top, right, bottom);
                                    }
                                } else {
                                    if (deltaY < 0) {
                                        if (!triggered) {
                                            triggered = true;

                                            // trigger single up !!!

                                            testText.setText("single up");
                                            MyVibrator.tick(requireActivity());
                                            top = rockerInitLayout[1] - MAX_MOVE;
                                            bottom = rockerInitLayout[3] - MAX_MOVE;
                                            rocker.layout(left, top, right, bottom);
                                        }
                                    } else {
                                        if (!triggered) {
                                            triggered = true;

                                            // trigger single down !!!

                                            testText.setText("single down");
                                            MyVibrator.tick(requireActivity());
                                            top = rockerInitLayout[1] + MAX_MOVE;
                                            bottom = rockerInitLayout[3] + MAX_MOVE;
                                            rocker.layout(left, top, right, bottom);
                                        }
                                    }
                                }
                            }
                        } else if (motionEvent.getPointerCount() == 2) {
                            // still possible to have only one finger
                            // catch exception to make sure the app wouldn't crash
                            try {
                                moveX2 = (int) motionEvent.getX(1) + rocker.getLeft();
                                moveY2 = (int) motionEvent.getY(1) + rocker.getTop();
                                // recover coordinate
                                int[] moveXY = transformCor(new int[]{moveX1, moveY1}, (int) rocker.getRotation(), rocker.getScaleX());
                                moveX1 = moveXY[0];
                                moveY1 = moveXY[1];
                                moveXY = transformCor(new int[]{moveX2, moveY2}, (int) rocker.getRotation(), rocker.getScaleX());
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
                                        if (Math.abs(deltaCenter) > DOUBLE_JUDGE_MOVE || Math.abs(deltaGap) > DOUBLE_JUDGE_MOVE) {
                                            int deltaX1 = moveX1 - startX1;
                                            int deltaY1 = moveY1 - startY1;
                                            int deltaX2 = moveX2 - startX2;
                                            int deltaY2 = moveY2 - startY2;
                                            if (Math.abs(deltaGap) > DOUBLE_JUDGE_MOVE && isHetero(deltaX1, deltaX2) && isHetero(deltaY1, deltaY2))
                                                mode = 1;
                                            else
                                                mode = 2;
                                        }
                                        break;
                                    // zoom in/out
                                    case 1:
                                        float rotation = (float) Math.toDegrees(Math.atan2(moveY2 - moveY1, moveX2 - moveX1)) - angle;
                                        if (rotation > 180)
                                            rotation = rotation - 360;
                                        if (rotation < -180)
                                            rotation = rotation + 360;
                                        rocker.setRotation(rotation);
                                        float scale = moveGap / startGap;
                                        if (scale >= MAX_SCALE) {
                                            if (!triggered) {
                                                triggered = true;

                                                // trigger zoom in !!!

                                                testText.setText("zoom in");
                                                MyVibrator.tick(requireActivity());
                                                rocker.setScaleX(MAX_SCALE);
                                                rocker.setScaleY(MAX_SCALE);
                                            }
                                        } else if (scale <= 1 / MAX_SCALE) {
                                            if (!triggered) {
                                                triggered = true;

                                                // trigger zoom out !!!

                                                testText.setText("zoom out");
                                                MyVibrator.tick(requireActivity());
                                                rocker.setScaleX(1 / MAX_SCALE);
                                                rocker.setScaleY(1 / MAX_SCALE);
                                            }
                                        } else {
                                            rocker.setScaleX(scale);
                                            rocker.setScaleY(scale);
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
                                            spinTop = rockerInitLayout[1];
                                            spinBottom = rockerInitLayout[3];
                                            if (Math.abs(deltaCenterX) < MAX_MOVE) {
                                                if (Math.abs(deltaCenterX) >= TAP_MOVE) {
                                                    spinLeft = (int) (rockerInitLayout[0] + (deltaCenterX * SPIN_MOVE_RATIO));
                                                    spinRight = (int) (rockerInitLayout[2] + (deltaCenterX * SPIN_MOVE_RATIO));
                                                    rocker.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                }
                                            } else {
                                                if (deltaCenterX < 0) {
                                                    if (!triggered) {
                                                        triggered = true;

                                                        // trigger double left !!!

                                                        testText.setText("double left");
                                                        MyVibrator.tick(requireActivity());
                                                        spinLeft = (int) (rockerInitLayout[0] - spinMaxMove);
                                                        spinRight = (int) (rockerInitLayout[2] - spinMaxMove);
                                                        rocker.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                    }
                                                } else {
                                                    if (!triggered) {
                                                        triggered = true;

                                                        // trigger double left !!!

                                                        testText.setText("double right");
                                                        MyVibrator.tick(requireActivity());
                                                        spinLeft = (int) (rockerInitLayout[0] + spinMaxMove);
                                                        spinRight = (int) (rockerInitLayout[2] + spinMaxMove);
                                                        rocker.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                    }
                                                }
                                            }

                                            rocker.setImageResource(rockerIcons[ICON_INDEX][1]);
                                            rocker.setTag("spin_x");
                                        } else {
                                            // on Y axis
                                            spinLeft = rockerInitLayout[0];
                                            spinRight = rockerInitLayout[2];
                                            if (Math.abs(deltaCenterY) < MAX_MOVE) {
                                                if (Math.abs(deltaCenterY) >= TAP_MOVE) {
                                                    spinTop = (int) (rockerInitLayout[1] + (deltaCenterY * SPIN_MOVE_RATIO));
                                                    spinBottom = (int) (rockerInitLayout[3] + (deltaCenterY * SPIN_MOVE_RATIO));
                                                    rocker.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                }
                                            } else {
                                                if (deltaCenterY < 0) {
                                                    if (!triggered) {
                                                        triggered = true;

                                                        // trigger double up !!!

                                                        testText.setText("double up");
                                                        MyVibrator.tick(requireActivity());
                                                        spinTop = (int) (rockerInitLayout[1] - spinMaxMove);
                                                        spinBottom = (int) (rockerInitLayout[3] - spinMaxMove);
                                                        rocker.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                    }
                                                } else {
                                                    if (!triggered) {
                                                        triggered = true;

                                                        // trigger double down !!!

                                                        testText.setText("double down");
                                                        MyVibrator.tick(requireActivity());
                                                        spinTop = (int) (rockerInitLayout[1] + spinMaxMove);
                                                        spinBottom = (int) (rockerInitLayout[3] + spinMaxMove);
                                                        rocker.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                    }
                                                }
                                            }
                                            rocker.setImageResource(rockerIcons[ICON_INDEX][2]);
                                            rocker.setTag("spin_y");
                                        }
                                        break;
                                }
                            } catch (IllegalArgumentException ignored) {}
                        }
                        break;

                    // second finger contact
                    case MotionEvent.ACTION_POINTER_DOWN:
                        startX2 = (int) motionEvent.getX(1) + rocker.getLeft();
                        startY2 = (int) motionEvent.getY(1) + rocker.getTop();
                        fingerNum = 2;
                        rocker.layout(rockerInitLayout[0], rockerInitLayout[1], rockerInitLayout[2], rockerInitLayout[3]);
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
                        if (fingerNum == 1 && dist(startX1, startY1, (int) motionEvent.getX() + rocker.getLeft(), (int) motionEvent.getY() + rocker.getTop()) < TAP_MOVE) {

                            // trigger single tap !!!

                            ScaleAnimation ta = new ScaleAnimation(1, 1.1F, 1, 1.1F, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                            ta.setDuration(MyAnimationScaler.getDuration(50, activity));
                            rocker.startAnimation(ta);
                            testText.setText("single tap");
                            MyVibrator.tick(requireActivity());
                        }

                        // reset params
                        fingerNum = 0;
                        triggered = false;
                        mode = 0;

                        // ball reset animations
                        if (rocker.getLeft() != rockerInitLayout[0] || rocker.getTop() != rockerInitLayout[1]) {
                            TranslateAnimation ta = new TranslateAnimation(0, rockerInitLayout[0] - rocker.getLeft(), 0, rockerInitLayout[1] - rocker.getTop());
                            ta.setDuration(MyAnimationScaler.getDuration(100, activity));
                            ta.setInterpolator(new OvershootInterpolator(3));
                            ta.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    rocker.clearAnimation();
                                    rocker.layout(rockerInitLayout[0], rockerInitLayout[1], rockerInitLayout[2], rockerInitLayout[3]);
                                }
                                @Override
                                public void onAnimationStart(Animation animation) {}
                                @Override
                                public void onAnimationRepeat(Animation animation) {}
                            });
                            rocker.startAnimation(ta);
                        }
                        if (rocker.getRotation() != 0 || rocker.getScaleX() != 1) {
                            RotateAnimation ra = new RotateAnimation(0, -rocker.getRotation(), Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            ra.setDuration(MyAnimationScaler.getDuration(100, activity));
                            ra.setInterpolator(new OvershootInterpolator(3));
                            ra.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {}
                                @Override
                                public void onAnimationEnd(Animation animation) { rocker.clearAnimation(); }
                                @Override
                                public void onAnimationRepeat(Animation animation) {}
                            });

                            ScaleAnimation sa = new ScaleAnimation(1, 1 / rocker.getScaleX(), 1, 1 / rocker.getScaleY(), Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                            sa.setDuration(MyAnimationScaler.getDuration(100, activity));
                            sa.setInterpolator(new OvershootInterpolator(3));
                            sa.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {}
                                @Override
                                public void onAnimationEnd(Animation animation) { rocker.clearAnimation(); }
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
                                    rocker.setRotation(0);
                                    rocker.setScaleX(1);
                                    rocker.setScaleY(1);
                                }
                                @Override
                                public void onAnimationRepeat(Animation animation) {}
                            });
                            rocker.startAnimation(as);
                        }
                        if (!rocker.getTag().equals("origin")) {
                            rocker.setImageResource(rockerIcons[ICON_INDEX][0]);
                            rocker.setTag("origin");
                        }
                        break;
                }
                return true;
            }
        });
    }

    public void setRockerParams(int[] fragmentSize, SharedPreferences pref) {
        int minSide = Math.min(fragmentSize[0], fragmentSize[1]);
        int travelSelectionIndex = pref.getInt("travel", 1);
        float spRatio = pref.getFloat("sm-ratio", 0.2F);
        float doubleJudgeRatio = pref.getFloat("double-judge-ratio", 0.04F);
        switch (travelSelectionIndex) {
            case 0:
                setMaxMove((int) (minSide * 0.1));
                setMaxScale(1.2F);
                break;
            case 1:
                setMaxMove((int) (minSide * 0.2));
                setMaxScale(1.5F);
                break;
            case 2:
                setMaxMove((int) (minSide * 0.3));
                setMaxScale(2F);
                break;
        }
        setSPRatio(spRatio);
        setDoubleJudgeMove(minSide * doubleJudgeRatio);
    }

    public void setMaxMove(int maxMove) {
        MAX_MOVE = maxMove;
    }

    public void setMaxScale(float maxScale) {
        MAX_SCALE = maxScale;
    }

    public void setSPRatio(float spRatio) {
        SPIN_MOVE_RATIO = spRatio;
    }

    public void setDoubleJudgeMove(float doubleJudgeMove) {
        DOUBLE_JUDGE_MOVE = doubleJudgeMove;
    }

    public float dist(int x1, int y1, int x2, int y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public int[] transformCor(int[] xy, int rotation, float scale) {
        int[] result = new int[2];
        int x1 = (int) (xy[0] * scale);
        int y1 = (int) (xy[1] * scale);
        result[0] = (int) ((x1) * Math.cos(Math.PI / 180.0 * rotation) - (y1) * Math.sin(Math.PI / 180.0 * rotation));
        result[1] = (int) ((x1) * Math.sin(Math.PI / 180.0 * rotation) + (y1) * Math.cos(Math.PI / 180.0 * rotation));
        return result;
    }

    public boolean isHetero(int a, int b) {
//        return a * b <= 0;
        return (a ^ b) >>> 31 == 1 || a == 0 || b == 0;
    }
}