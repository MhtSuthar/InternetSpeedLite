package com.internetspeedlite.base;

import android.app.Application;

import com.internetspeedlite.storage.SharedPreferenceUtil;

/**
 * Created by AND001 on 6/24/2017.
 */

public class InternetSpeedApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferenceUtil.init(this);
    }
}
