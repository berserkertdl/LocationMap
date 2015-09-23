package com.location.map.helper.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2015/9/15 0015.
 */
public class T{

    public static boolean isDebug = false;

    public static void makeText(Context context, CharSequence text, int duration) {
        if(isDebug)
            Toast.makeText(context, text, duration).show();
    }

}
