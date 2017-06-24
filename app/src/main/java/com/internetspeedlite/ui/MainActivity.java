package com.internetspeedlite.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;

import com.github.anastr.speedviewlib.SpeedView;
import com.internetspeedlite.R;
import com.internetspeedlite.base.BaseActivity;
import com.internetspeedlite.listner.OnSpeedConnected;
import com.internetspeedlite.service.SpeedCalService;
import com.internetspeedlite.storage.SharedPreferenceUtil;
import com.internetspeedlite.utilz.AnimationUtils;
import com.internetspeedlite.utilz.AppUtilz;
import com.internetspeedlite.utilz.Constants;

public class MainActivity extends BaseActivity implements OnSpeedConnected {

    private SpeedView mSpeedMeter;
    SpeedCalService mService;
    boolean mBound = false;
    private static final String TAG = "MainActivity";
    private AppCompatButton mNotificationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mSpeedMeter = (SpeedView) findViewById(R.id.speedView);
        mSpeedMeter.setMaxSpeed(5000);
        mSpeedMeter.setSpeedTextTypeface(AppUtilz.getCustomFont(this));
        mNotificationSwitch = (AppCompatButton) findViewById(R.id.notification_card);
        mNotificationSwitch.setTypeface(AppUtilz.getCustomFontBold(this));
        mNotificationSwitch.setText(SharedPreferenceUtil.getBoolean(Constants.KEY_IS_NOTIFICATI_ON, true) ? "Notification is ON" : "Notification is OFF");
    }

    public void onNotificationSwitchClick(View view){
        if(SharedPreferenceUtil.getBoolean(Constants.KEY_IS_NOTIFICATI_ON, true)){
            SharedPreferenceUtil.putValue(Constants.KEY_IS_NOTIFICATI_ON, false);
            SharedPreferenceUtil.save();
            onStop();
            stopSpeedService();
        }else{
            SharedPreferenceUtil.putValue(Constants.KEY_IS_NOTIFICATI_ON, true);
            SharedPreferenceUtil.save();
            onStart();
        }
        mNotificationSwitch.setText(SharedPreferenceUtil.getBoolean(Constants.KEY_IS_NOTIFICATI_ON, true) ? "Notification is ON" : "Notification is OFF");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(SharedPreferenceUtil.getBoolean(Constants.KEY_IS_NOTIFICATI_ON, true))
            startSpeedService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    void startSpeedService(){
        Intent mService = new Intent(this, SpeedCalService.class);
        bindService(mService, mConnection, Context.BIND_AUTO_CREATE);
        startService(mService);
    }

    void stopSpeedService(){
        Intent mService = new Intent(this, SpeedCalService.class);
        stopService(mService);
    }

    @Override
    public void speedCount(Long speed, String unit) {
        mSpeedMeter.speedTo(speed >= 1000000 ? speed : (float) speed / (float) 1000);
        mSpeedMeter.setUnit(" KB/s");
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpeedCalService.LocalBinder binder = (SpeedCalService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            initializeListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void initializeListener() {
        mService.setListener(this);
    }

}
