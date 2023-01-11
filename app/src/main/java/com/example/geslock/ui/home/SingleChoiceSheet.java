package com.example.geslock.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.geslock.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

public class SingleChoiceSheet extends BottomSheetDialog{

    private final Activity activity;
    private final SharedPreferences pref;

    private final TextView[] tvEntries;
    private final ImageView[] imgChecks;

    private final int originalTextColor;

    public SingleChoiceSheet(Activity activity, int contentViewId, int[] entryIds, int[] checkIds) {
        super(activity);
        // set activity
        this.activity = activity;
        // get preferences
        pref = activity.getSharedPreferences("pref", Context.MODE_PRIVATE);

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

        // set original color
        originalTextColor = Objects.requireNonNull(tvEntries[0]).getCurrentTextColor();

        initChoice();

        for (int i = 0; i < tvEntries.length; i++) {
            int finalI = i;
            tvEntries[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setChoice(finalI);
                }
            });
        }
    }

    public void initChoice() {

    }

    public void setChoice(int index) {
        // not changed
        if (imgChecks[index].getVisibility() == View.VISIBLE) return;
        // changed
        synchronized (this) {
            for (int i = 0; i < tvEntries.length; i++) {
                if (i == index) {
                    tvEntries[i].setTextColor(activity.getColor(R.color.yellow_500));
                    tvEntries[i].setTypeface(null, Typeface.BOLD);
                    imgChecks[i].setVisibility(View.VISIBLE);
                } else {
                    if (imgChecks[i].getVisibility() == View.VISIBLE) {
                        tvEntries[i].setTextColor(originalTextColor);
                        tvEntries[i].setTypeface(null, Typeface.NORMAL);
                        imgChecks[i].setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
    }
}
