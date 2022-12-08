package com.example.geslock.tools;

import android.app.Activity;
import android.widget.Toast;

public abstract class MyToastMaker {

    private static Toast toast;

    public static void make(String text, Activity activity) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(activity, text, Toast.LENGTH_LONG);
        toast.show();
    }
}
