package com.internetspeedlite.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.github.anastr.speedviewlib.SpeedView;
import com.internetspeedlite.R;
import com.internetspeedlite.base.BaseActivity;
import com.internetspeedlite.listner.OnSpeedConnected;
import com.internetspeedlite.service.SpeedCalService;
import com.internetspeedlite.utilz.AppUtilz;

public class MainActivity extends BaseActivity implements OnSpeedConnected {

    private SpeedView mSpeedMeter;
    SpeedCalService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mSpeedMeter = (SpeedView) findViewById(R.id.speedView);
        //mSpeedMeter.speedTo(50, 4000);
        mSpeedMeter.setMaxSpeed(1000);
        mSpeedMeter.setSpeedTextTypeface(AppUtilz.getCustomFont(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        startSpeedService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
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

    @Override
    public void speedCount(String speed, String unit) {
        mSpeedMeter.speedTo(Float.parseFloat(speed));
        mSpeedMeter.setUnit(unit);
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
