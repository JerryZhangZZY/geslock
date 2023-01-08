package com.example.geslock.ui.home;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.example.geslock.R;
import com.example.geslock.tools.MyAnimationScaler;
import com.example.geslock.tools.MyDefaultPref;
import com.example.geslock.tools.MyVibrator;

public class RockerDialog {

    private final int MAX_PASSWORD_LENGTH = 10;

    private final Activity activity;
    private final SharedPreferences pref;
    private final Dialog dialog;

    private int MAX_MOVE = 300;
    private float MAX_SCALE = 1.5F;
    private final int ICON_INDEX;
    private float SPIN_MOVE_RATIO = .2F;
    private float DOUBLE_JUDGE_MOVE = 43.2F;
    private int TAP_MOVE = 10;

    private final Interpolator interpolator;
    private final AlphaAnimation appearAnimation;
    private final AlphaAnimation fadeAnimation;
    private final ValueAnimator cardToRedAnimation;
    private final ValueAnimator cardFromRedAnimation;
    private final ValueAnimator textToRedAnimation;
    private final ValueAnimator textFromRedAnimation;
    private final SpringAnimation shakeAnimation;
    private final float STIFFNESS;
    private final int ANIM_DURATION_100;
    private final int ANIM_DURATION_50;

    private final int[] dialogSize = new int[2];
    private final int[][] rockerIcons = new int[3][3];
    private final int[] rockerInitLayout = new int[4];
    private final TextView tvPassword;
    private final ImageButton btnBackspace;
    private final CardView cardPassword;
    private final CardView cardPasswordText;
    private final ImageView rocker;
    private final ImageView cross;
    private final Button btnPositive;
    private final Button btnNegative;
    private final TextView tvPasswordHint;

    private final int red_200;
    private final int red_500;
    private final int origin;

    @SuppressLint("ClickableViewAccessibility")
    public RockerDialog(Activity activity) {
        // set activity
        this.activity = activity;
        // get preferences
        assert activity != null;
        pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE);

        // set dialog
        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_rocker);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rocker_dialog_background);
        setDialogSize();
        dialog.getWindow().setLayout(dialogSize[0], dialogSize[1]);

        // set icon selection
        ICON_INDEX = pref.getInt("icon", MyDefaultPref.getDefaultInt("icon"));

        // set drag params
        setRockerParams(dialogSize, pref);

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
        tvPassword = dialog.findViewById(R.id.tvPassword);
        btnBackspace = dialog.findViewById(R.id.btnBackspace);
        cardPassword = dialog.findViewById(R.id.cardPassword);
        cardPasswordText = dialog.findViewById(R.id.cardPasswordText);
        rocker = dialog.findViewById(R.id.rocker);
        cross = dialog.findViewById(R.id.cross);
        btnPositive = dialog.findViewById(R.id.btnRockerPositive);
        btnNegative = dialog.findViewById(R.id.btnRockerNegative);
        tvPasswordHint = dialog.findViewById(R.id.tvPasswordHint);

        // get colors
        red_200 = activity.getColor(R.color.red_200);
        red_500 = activity.getColor(R.color.red_500);
        origin = cardPassword.getCardBackgroundColor().getDefaultColor();

        // set animations
        interpolator = new OvershootInterpolator(pref.getFloat("overshoot", MyDefaultPref.getDefaultFloat("overshoot")) * 10);
        ANIM_DURATION_100 = MyAnimationScaler.getDuration(100, activity);
        ANIM_DURATION_50 = MyAnimationScaler.getDuration(50, activity);
        appearAnimation = new AlphaAnimation(0, 1);
        appearAnimation.setDuration(ANIM_DURATION_100);
        fadeAnimation = new AlphaAnimation(1, 0);
        fadeAnimation.setDuration(ANIM_DURATION_100);
        cardToRedAnimation = ValueAnimator.ofInt(cardPassword.getCardBackgroundColor().getDefaultColor(), red_200);
        cardToRedAnimation.setEvaluator(new ArgbEvaluator());
        cardToRedAnimation.addUpdateListener(valueAnimator -> cardPassword.setCardBackgroundColor((int) valueAnimator.getAnimatedValue()));
        cardToRedAnimation.setDuration(ANIM_DURATION_50);
        cardFromRedAnimation = ValueAnimator.ofInt(red_200, origin);
        cardFromRedAnimation.setEvaluator(new ArgbEvaluator());
        cardFromRedAnimation.addUpdateListener(valueAnimator -> cardPassword.setCardBackgroundColor((int) valueAnimator.getAnimatedValue()));
        cardFromRedAnimation.setDuration(ANIM_DURATION_50);
        textToRedAnimation = ValueAnimator.ofInt(tvPassword.getTextColors().getDefaultColor(), red_500);
        textToRedAnimation.setEvaluator(new ArgbEvaluator());
        textToRedAnimation.addUpdateListener(valueAnimator -> tvPassword.setTextColor((int) valueAnimator.getAnimatedValue()));
        textToRedAnimation.setDuration(ANIM_DURATION_100);
        textFromRedAnimation = ValueAnimator.ofInt(red_500, tvPassword.getTextColors().getDefaultColor());
        textFromRedAnimation.setEvaluator(new ArgbEvaluator());
        textFromRedAnimation.addUpdateListener(valueAnimator -> tvPassword.setTextColor((int) valueAnimator.getAnimatedValue()));
        textFromRedAnimation.setDuration(ANIM_DURATION_100);
        STIFFNESS = MyAnimationScaler.getSpringStiffness(5000F, activity);
        shakeAnimation = new SpringAnimation(cardPassword, SpringAnimation.TRANSLATION_X, 0)
                .setSpring(new SpringForce(0).setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY).setStiffness(STIFFNESS));

        cross.setVisibility(pref.getBoolean("cross", MyDefaultPref.getDefaultBoolean("cross")) ? View.VISIBLE : View.INVISIBLE);

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

            @SuppressLint("SetTextI18n")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    // first finger contact
                    case MotionEvent.ACTION_DOWN:
                        rocker.clearAnimation();
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
                                            tvPassword.setText(tvPassword.getText() + "B");

                                            MyVibrator.tick(activity);
                                            left = rockerInitLayout[0] - MAX_MOVE;
                                            right = rockerInitLayout[2] - MAX_MOVE;
                                            rocker.layout(left, top, right, bottom);
                                        }
                                    } else {
                                        if (!triggered) {
                                            triggered = true;

                                            // trigger single right !!!
                                            tvPassword.setText(tvPassword.getText() + "C");

                                            MyVibrator.tick(activity);
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
                                            tvPassword.setText(tvPassword.getText() + "D");

                                            MyVibrator.tick(activity);
                                            top = rockerInitLayout[1] - MAX_MOVE;
                                            bottom = rockerInitLayout[3] - MAX_MOVE;
                                            rocker.layout(left, top, right, bottom);
                                        }
                                    } else {
                                        if (!triggered) {
                                            triggered = true;

                                            // trigger single down !!!
                                            tvPassword.setText(tvPassword.getText() + "E");

                                            MyVibrator.tick(activity);
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
                                                tvPassword.setText(tvPassword.getText() + "J");

                                                MyVibrator.tick(activity);
                                                rocker.setScaleX(MAX_SCALE);
                                                rocker.setScaleY(MAX_SCALE);
                                            }
                                        } else if (scale <= 1 / MAX_SCALE) {
                                            if (!triggered) {
                                                triggered = true;

                                                // trigger zoom out !!!
                                                tvPassword.setText(tvPassword.getText() + "K");

                                                MyVibrator.tick(activity);
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
                                                        tvPassword.setText(tvPassword.getText() + "F");

                                                        MyVibrator.tick(activity);
                                                        spinLeft = (int) (rockerInitLayout[0] - spinMaxMove);
                                                        spinRight = (int) (rockerInitLayout[2] - spinMaxMove);
                                                        rocker.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                    }
                                                } else {
                                                    if (!triggered) {
                                                        triggered = true;

                                                        // trigger double left !!!
                                                        tvPassword.setText(tvPassword.getText() + "G");

                                                        MyVibrator.tick(activity);
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
                                                        tvPassword.setText(tvPassword.getText() + "H");

                                                        MyVibrator.tick(activity);
                                                        spinTop = (int) (rockerInitLayout[1] - spinMaxMove);
                                                        spinBottom = (int) (rockerInitLayout[3] - spinMaxMove);
                                                        rocker.layout(spinLeft, spinTop, spinRight, spinBottom);
                                                    }
                                                } else {
                                                    if (!triggered) {
                                                        triggered = true;

                                                        // trigger double down !!!
                                                        tvPassword.setText(tvPassword.getText() + "I");

                                                        MyVibrator.tick(activity);
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
                            } catch (IllegalArgumentException ignored) {
                            }
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

                    // all fingers leave
                    case MotionEvent.ACTION_UP:
                        // single finger tap
                        if (fingerNum == 1 && dist(startX1, startY1, (int) motionEvent.getX() + rocker.getLeft(), (int) motionEvent.getY() + rocker.getTop()) < TAP_MOVE) {

                            // trigger single tap !!!
                            tvPassword.setText(tvPassword.getText() + "A");

                            ScaleAnimation ta = new ScaleAnimation(1.1F, 1, 1.1F, 1, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
                            ta.setDuration(ANIM_DURATION_50);
                            rocker.startAnimation(ta);
                            MyVibrator.tick(activity);
                        }

                        // reset params
                        fingerNum = 0;
                        triggered = false;
                        mode = 0;

                        // ball reset animations
                        if (rocker.getLeft() != rockerInitLayout[0] || rocker.getTop() != rockerInitLayout[1]) {
                            TranslateAnimation ta = new TranslateAnimation(0, rockerInitLayout[0] - rocker.getLeft(), 0, rockerInitLayout[1] - rocker.getTop());
                            ta.setDuration(ANIM_DURATION_100);
                            ta.setInterpolator(interpolator);
                            ta.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    rocker.clearAnimation();
                                    rocker.layout(rockerInitLayout[0], rockerInitLayout[1], rockerInitLayout[2], rockerInitLayout[3]);
                                }

                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }
                            });
                            rocker.startAnimation(ta);
                        }
                        if (rocker.getRotation() != 0 || rocker.getScaleX() != 1) {
                            RotateAnimation ra = new RotateAnimation(0, -rocker.getRotation(), Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
                            ra.setDuration(ANIM_DURATION_100);
                            ra.setInterpolator(interpolator);
                            ra.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    rocker.clearAnimation();
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }
                            });

                            ScaleAnimation sa = new ScaleAnimation(1, 1 / rocker.getScaleX(), 1, 1 / rocker.getScaleY(), Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
                            sa.setDuration(ANIM_DURATION_100);
                            sa.setInterpolator(interpolator);
                            sa.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    rocker.clearAnimation();
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }
                            });

                            AnimationSet as = new AnimationSet(false);
                            as.addAnimation(ra);
                            as.addAnimation(sa);
                            as.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    rocker.setRotation(0);
                                    rocker.setScaleX(1);
                                    rocker.setScaleY(1);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }
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

        // set password card height
        cardPasswordText.post(() -> cardPasswordText.setMinimumWidth(cardPasswordText.getHeight()));

        // set password backspace
        btnBackspace.setOnClickListener(view -> {
            CharSequence password = tvPassword.getText();
            if (password.length() > 0) {
                MyVibrator.tick(activity);
                tvPassword.setText(password.subSequence(0, password.length() - 1));
            }
        });
        btnBackspace.setOnLongClickListener(view -> {
            tvPassword.setText(null);
            return true;
        });

        // set password text box animation
        tvPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int length = charSequence.length();
                if (length > 0) {
                    if (length > MAX_PASSWORD_LENGTH) {
                        // limit password length
                        cardToRedAnimation.start();
                        new Handler().postDelayed(() -> {
                            // prevent over delete
                            if (tvPassword.length() > MAX_PASSWORD_LENGTH) {
                                btnBackspace.callOnClick();
                                cardToRedAnimation.cancel();
                                cardFromRedAnimation.start();
                            }
                        }, ANIM_DURATION_50);
                    }
                    if (cardPassword.getVisibility() != View.VISIBLE) {
                        cardPassword.startAnimation(appearAnimation);
                        cardPassword.setVisibility(View.VISIBLE);
                    }
                    if (!btnPositive.isEnabled()) {
                        btnPositive.animate().alpha(1).setDuration(ANIM_DURATION_100);
                        btnPositive.setEnabled(true);
                    }
                    if (tvPasswordHint.getVisibility() != View.INVISIBLE) {
                        tvPasswordHint.startAnimation(fadeAnimation);
                        tvPasswordHint.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (cardPassword.getVisibility() != View.INVISIBLE) {
                        cardPassword.startAnimation(fadeAnimation);
                        cardPassword.setVisibility(View.INVISIBLE);
                    }
                    if (btnPositive.isEnabled()) {
                        btnPositive.animate().alpha(.5F).setDuration(ANIM_DURATION_100);
                        btnPositive.setEnabled(false);
                    }
                    if (tvPasswordHint.getVisibility() != View.VISIBLE) {
                        new Handler().postDelayed(() -> {
                            tvPasswordHint.startAnimation(appearAnimation);
                            tvPasswordHint.setVisibility(View.VISIBLE);
                        }, ANIM_DURATION_100);
                    }
                }
                int currentWidth = tvPassword.getWidth();
                tvPassword.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                int targetWidth = Math.min(tvPassword.getMeasuredWidth(), dialogSize[0] - 300);
                ValueAnimator lengthAnimator = ValueAnimator.ofInt(currentWidth, targetWidth);
                lengthAnimator.addUpdateListener(animation -> {
                    tvPassword.getLayoutParams().width = (int) animation.getAnimatedValue();
                    tvPassword.requestLayout();
                });
                lengthAnimator.setInterpolator(interpolator);
                lengthAnimator.setDuration(ANIM_DURATION_100);
                lengthAnimator.start();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        btnNegative.setOnClickListener(v -> dialog.dismiss());
    }

    /**
     * Calculate dialog size and set params.
     */
    public void setDialogSize() {
        int maxWidth = dpToPx(400);
        float widthPercentage = .9F;
        float widthHeightRatio = .7F;
        int width = (int) Math.min(widthPercentage * activity.getWindow().getDecorView().getWidth(), maxWidth);
        int height = (int) Math.min(width / widthHeightRatio, widthPercentage * activity.getWindow().getDecorView().getHeight());
        dialogSize[0] = width;
        dialogSize[1] = height;
    }

    /**
     * Convert dp to px.
     *
     * @param dp dp
     * @return px
     */
    public int dpToPx(int dp) {
        float density = activity.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    /**
     * Calculate and set params for rocker.
     *
     * @param dialogSize size array
     * @param pref       shared preference
     */
    public void setRockerParams(int[] dialogSize, SharedPreferences pref) {
        int minSide = Math.min(dialogSize[0], dialogSize[1]);
        int travelSelectionIndex = pref.getInt("travel", MyDefaultPref.getDefaultInt("travel"));
        float spRatio = pref.getFloat("sm-ratio", MyDefaultPref.getDefaultFloat("sm-ratio"));
        float doubleJudgeRatio = pref.getFloat("tm-ratio", MyDefaultPref.getDefaultFloat("tm-ratio"));

        switch (travelSelectionIndex) {
            case 0:
                setMaxMove((int) (minSide * .1));
                setMaxScale(1.2F);
                break;
            case 1:
                setMaxMove((int) (minSide * .2));
                setMaxScale(1.5F);
                break;
            case 2:
                setMaxMove((int) (minSide * .3));
                setMaxScale(2F);
                break;
        }
        setSPRatio(spRatio);
        setDoubleJudgeMove(minSide * doubleJudgeRatio);
        setTapMove((int) (minSide * .01));
    }

    /**
     * Calculate the distance between two points.
     *
     * @param x1 x of the point one
     * @param y1 y of point one
     * @param x2 x of point two
     * @param y2 y of point two
     * @return distance
     */
    public float dist(int x1, int y1, int x2, int y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * Recover the rotated coordinates.
     *
     * @param xy       array of original x,y of the point
     * @param rotation rotation angle
     * @param scale    scale ratio
     * @return recovered coordinates
     */
    public int[] transformCor(int[] xy, int rotation, float scale) {
        int[] result = new int[2];
        int x1 = (int) (xy[0] * scale);
        int y1 = (int) (xy[1] * scale);
        result[0] = (int) ((x1) * Math.cos(Math.PI / 180.0 * rotation) - (y1) * Math.sin(Math.PI / 180.0 * rotation));
        result[1] = (int) ((x1) * Math.sin(Math.PI / 180.0 * rotation) + (y1) * Math.cos(Math.PI / 180.0 * rotation));
        return result;
    }

    /**
     * Determine if two numbers have different signs.
     *
     * @param a number one
     * @param b number two
     * @return result
     */
    public boolean isHetero(int a, int b) {
        return (a ^ b) >>> 31 == 1 || a == 0 || b == 0;
    }

    /**
     * Reactions when password wrong.
     */
    public void handleWrongPassword() {
        textToRedAnimation.start();
        // animation enabled
        if (STIFFNESS != 524) {
            shakeAnimation.setStartValue(50);
            shakeAnimation.addEndListener((animation, canceled, value, velocity) -> {
                textFromRedAnimation.start();
                tvPassword.setText(null);
            });
            shakeAnimation.start();
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    textFromRedAnimation.start();
                    tvPassword.setText(null);
                }
            }, 500);
        }
        MyVibrator.shake(activity);
    }

    /**
     * Show the dialog.
     */
    public void show() {
        dialog.show();
    }

    /**
     * Dismiss the dialog.
     */
    public void dismiss() {
        dialog.dismiss();
    }

    public Button getBtnPositive() {
        return btnPositive;
    }

    public String getPassword() {
        return String.valueOf(tvPassword.getText());
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

    public void setTapMove(int tapMove) {
        TAP_MOVE = tapMove;
    }
}
