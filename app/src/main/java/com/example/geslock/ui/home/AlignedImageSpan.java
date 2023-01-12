package com.example.geslock.ui.home;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

public class AlignedImageSpan extends ImageSpan {

    public AlignedImageSpan(Drawable d) {
        super(d);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint
            paint) {
        Drawable drawable = getDrawable();
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        int transY = (int) ((y + fontMetrics.ascent + y + fontMetrics.descent) / 2 - (drawable.getBounds().bottom + drawable
                .getBounds().top) / 2);
        canvas.save();
        canvas.translate(x, transY);
        drawable.draw(canvas);
        canvas.restore();
    }
}

