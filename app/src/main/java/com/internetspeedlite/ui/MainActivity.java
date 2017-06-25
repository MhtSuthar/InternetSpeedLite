package com.internetspeedlite.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import com.github.anastr.speedviewlib.SpeedView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.internetspeedlite.R;
import com.internetspeedlite.base.BaseActivity;
import com.internetspeedlite.listner.OnSpeedConnected;
import com.internetspeedlite.service.SpeedCalService;
import com.internetspeedlite.storage.SharedPreferenceUtil;
import com.internetspeedlite.utilz.AppUtilz;
import com.internetspeedlite.utilz.Constants;

import static com.internetspeedlite.utilz.AppUtilz.isOnline;

public class MainActivity extends BaseActivity implements OnSpeedConnected {

    private SpeedView mSpeedMeter;
    SpeedCalService mService;
    boolean mBound = false;
    private static final String TAG = "MainActivity";
    private AppCompatButton mNotificationSwitch;

    /**
     *
     * Initialize ads here
     */
    private AdView adView;
    private InterstitialAd mInterstitialAd;
    private boolean mFullAddDisplayed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        adView = (AdView) findViewById(R.id.adView);
        mSpeedMeter = (SpeedView) findViewById(R.id.speedView);
        mSpeedMeter.setMaxSpeed(5000);
        mSpeedMeter.setSpeedTextTypeface(AppUtilz.getCustomFont(this));
        mNotificationSwitch = (AppCompatButton) findViewById(R.id.notification_card);
        mNotificationSwitch.setTypeface(AppUtilz.getCustomFontBold(this));
        mNotificationSwitch.setText(SharedPreferenceUtil.getBoolean(Constants.KEY_IS_NOTIFICATI_ON, true) ? "Notification is ON" : "Notification is OFF");
        initAds();
    }

    private void initAds() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();

       /* AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("293A346CE5B912415358FE79AB75E057")  // My Galaxy Nexus test phone
                .build();*/

        adView.loadAd(adRequest);

        if(!isOnline(this))
            adView.setVisibility(View.GONE);

        mInterstitialAd = new InterstitialAd(this);

        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {

            }
        });
    }

    public void onNotificationSwitchClick(View view){
        if(SharedPreferenceUtil.getBoolean(Constants.KEY_IS_NOTIFICATI_ON, true)){
            SharedPreferenceUtil.putValue(Constants.KEY_IS_NOTIFICATI_ON, false);
            SharedPreferenceUtil.save();
            if(mService != null){
                mService.setNotificationStop();
            }
        }else{
            if(mService != null){
                mService.setNotificationStart();
            }
            SharedPreferenceUtil.putValue(Constants.KEY_IS_NOTIFICATI_ON, true);
            SharedPreferenceUtil.save();
        }
        mNotificationSwitch.setText(SharedPreferenceUtil.getBoolean(Constants.KEY_IS_NOTIFICATI_ON, true) ? "Notification is ON" : "Notification is OFF");
    }

    @Override
    protected void onStart() {
        super.onStart();
        //if(SharedPreferenceUtil.getBoolean(Constants.KEY_IS_NOTIFICATI_ON, true))
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
        if (mInterstitialAd.isLoaded() && !mFullAddDisplayed) {
            mInterstitialAd.show();
            mFullAddDisplayed = true;
        }else
            finish();
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
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
