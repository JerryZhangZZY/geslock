package com.example.geslock.tools;

import android.content.Context;
import android.util.TypedValue;

public abstract class MyPixelConverter {

    /**
     * Convert dp to px.
     *
     * @param dp dp
     * @param context context
     * @return px
     */
    public static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    /**
     * Convert sp to px.
     *
     * @param sp sp
     * @param context context
     * @return px
     */
    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }
}
