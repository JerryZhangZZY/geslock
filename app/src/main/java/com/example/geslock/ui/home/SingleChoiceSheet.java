package com.example.geslock.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.geslock.R;
import com.example.geslock.tools.MyDefaultPref;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

public class SingleChoiceSheet extends BottomSheetDialog {

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private final TextView[] tvEntries;
    private final ImageView[] imgChecks;
    private final ImageView[] imgIcons;
    private final String prefName;

    private final int originalTextColor;
    private final int yellow_500;

    private boolean changed = false;

    public SingleChoiceSheet(Activity activity, int contentViewId, int[] entryIds, int[] checkIds, int[] iconIds, String prefName) {
        super(activity);
        // get preferences
        pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = pref.edit();

        // set sheet
        this.setContentView(contentViewId);

        // set views
        tvEntries = new TextView[entryIds.length];
        for (int i = 0; i < tvEntries.length; i++) {
            tvEntries[i] = this.findViewById(entryIds[i]);
        }
        imgChecks = new ImageView[checkIds.length];
        for (int i = 0; i < imgChecks.length; i++) {
            imgChecks[i] = this.findViewById(checkIds[i]);
        }
        imgIcons = new ImageView[iconIds.length];
        for (int i = 0; i < imgIcons.length; i++) {
            imgIcons[i] = this.findViewById(iconIds[i]);
        }

        // set preference name
        this.prefName = prefName;

        // set colors
        originalTextColor = Objects.requireNonNull(tvEntries[0]).getCurrentTextColor();
        yellow_500 = activity.getColor(R.color.yellow_500);

        // init current choice
        initChoice();

        // set listeners
        for (int i = 0; i < tvEntries.length; i++) {
            int finalI = i;
            tvEntries[i].setOnClickListener(v -> {
                setChoice(finalI);
                SingleChoiceSheet.super.dismiss();
            });
        }
    }

    /**
     * Set current choice from shared preferences.
     */
    public void initChoice() {
        setChecked(pref.getInt(prefName, MyDefaultPref.getDefaultInt(prefName)));
    }

    /**
     * Set choice and save to shared preferences.
     * @param index choice index
     */
    public void setChoice(int index) {
        // not changed
        if (imgChecks[index].getVisibility() == View.VISIBLE) return;
        // changed
        synchronized (this) {
            for (int i = 0; i < tvEntries.length; i++) {
                if (i == index) {
                    setChecked(i);
                } else {
                    setUnchecked(i);
                }
            }
            changed = true;
            editor.putInt(prefName, index);
            editor.commit();
        }
    }

    /**
     * Set choice.
     * @param index choice index
     */
    public void setChecked(int index) {
        tvEntries[index].setTextColor(yellow_500);
        tvEntries[index].setTypeface(null, Typeface.BOLD);
        imgChecks[index].setVisibility(View.VISIBLE);
        imgIcons[index].setColorFilter(yellow_500);
    }

    /**
     * Reset other entries.
     * @param index choice index
     */
    public void setUnchecked(int index) {
        if (imgChecks[index].getVisibility() == View.VISIBLE) {
            tvEntries[index].setTextColor(originalTextColor);
            tvEntries[index].setTypeface(null, Typeface.NORMAL);
            imgChecks[index].setVisibility(View.INVISIBLE);
            imgIcons[index].setColorFilter(originalTextColor);
        }
    }

    public boolean changed() {
        return changed;
    }
}
