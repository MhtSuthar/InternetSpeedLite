package com.internetspeedlite.utilz;

import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * Created by Admin on 23-Jun-17.
 */

public class AppUtilz {

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Typeface getCustomFont(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/Quicksand-Regular.ttf");
    }

    public static Typeface getCustomFontBold(Context context){
        return Typeface.createFromAsset(context.getAssets(), "fonts/Quicksand-Bold.ttf");
    }
}
