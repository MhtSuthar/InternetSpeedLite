package com.internetspeedlite.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.internetspeedlite.R;
import com.internetspeedlite.listner.OnSpeedConnected;
import com.internetspeedlite.ui.MainActivity;
import com.internetspeedlite.utilz.AppUtilz;

import java.util.Locale;

/**
 * Created by mht on 9/15/2016.
 */
public class SpeedCalService extends IntentService {

    private final int mNotificationId = 1;
    private Handler mHandler;
    private Notification.Builder mBuilder;
    private NotificationManager mNotifyMgr;
    private String mDownloadSpeedOutput;
    private long mDownloadSpeed;
    private String mUnits;
    private boolean mDestroyed = false;
    private final IBinder mBinder = new LocalBinder();
    private OnSpeedConnected onSpeedConnected;
    private static final String TAG = "SpeedCalService";

    public SpeedCalService() {
        super("SpeedCalService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setListener(OnSpeedConnected onSpeedConnected) {
        this.onSpeedConnected = onSpeedConnected;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        initializeNotification();

        while (!mDestroyed) {
            getDownloadSpeed();

            Message completeMessage = mHandler.obtainMessage(1);
            completeMessage.sendToTarget();
        }
    }

    private void initializeNotification() {

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                if (mDestroyed) {
                    return;
                }
                Bitmap bitmap = createBitmapFromString(mDownloadSpeedOutput, mUnits);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mBuilder.setSmallIcon(Icon.createWithBitmap(bitmap));
                } else {
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                }
                mBuilder.setContentText(getString(R.string.app_name));
                mBuilder.setContentTitle("Speed: " + mDownloadSpeedOutput + " " + mUnits);
                mNotifyMgr.notify(mNotificationId, mBuilder.build());
                if (onSpeedConnected != null) {
                    onSpeedConnected.speedCount(mDownloadSpeed, mUnits);
                }
            }
        };


        mBuilder = new Notification.Builder(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBuilder.setSmallIcon(Icon.createWithBitmap(createBitmapFromString("0", " KB/s")));
            mBuilder.setVisibility(Notification.VISIBILITY_SECRET);
            mBuilder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        } else {
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        }
        mBuilder.setContentText(getString(R.string.app_name));
        mBuilder.setContentTitle("Speed: " + mDownloadSpeedOutput + " " + " KB/s");

        mBuilder.setPriority(Notification.PRIORITY_HIGH);
        mBuilder.setOngoing(true);
        //mBuilder.f = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR

        /*Creates a special PendingIntent so that the app will open when the notification window
        is tapped*/
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent
                .FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);


        mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

        startForeground(mNotificationId, mBuilder.build());
    }

    private Bitmap createBitmapFromString(String speed, String units) {

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(70);
        paint.setTypeface(AppUtilz.getCustomFontBold(getApplicationContext()));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);

        Paint unitsPaint = new Paint();
        unitsPaint.setAntiAlias(true);
        unitsPaint.setTextSize(40); // size is in pixels
        unitsPaint.setTypeface(AppUtilz.getCustomFont(getApplicationContext()));
        unitsPaint.setTextAlign(Paint.Align.CENTER);
        unitsPaint.setColor(Color.WHITE);

        Rect textBounds = new Rect();
        paint.getTextBounds(speed, 0, speed.length(), textBounds);

        Rect unitsTextBounds = new Rect();
        unitsPaint.getTextBounds(units, 0, units.length(), unitsTextBounds);

        int width = (textBounds.width() > unitsTextBounds.width()) ? textBounds.width() : unitsTextBounds.width();

        Bitmap bitmap = Bitmap.createBitmap(width + 10, 90,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(speed, width / 2 + 5, 50, paint);
        canvas.drawText(units, width / 2, 90, unitsPaint);

        return bitmap;
    }

    private void getDownloadSpeed() {

        long mRxBytesPrevious = TrafficStats.getTotalRxBytes();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long mRxBytesCurrent = TrafficStats.getTotalRxBytes();

        mDownloadSpeed = mRxBytesCurrent - mRxBytesPrevious;

        float mDownloadSpeedWithDecimals;

        if (mDownloadSpeed >= 1000000000) {
            mDownloadSpeedWithDecimals = (float) mDownloadSpeed / (float) 1000000000;
            mUnits = " GB/s";
        } else if (mDownloadSpeed >= 1000000) {
            mDownloadSpeedWithDecimals = (float) mDownloadSpeed / (float) 1000000;
            mUnits = " MB/s";

        } else {
            mDownloadSpeedWithDecimals = (float) mDownloadSpeed / (float) 1000;
            mUnits = " KB/s";
        }

        if (!mUnits.contains(" KB") && mDownloadSpeedWithDecimals < 100) {
            mDownloadSpeedOutput = String.format(Locale.US, "%.1f", mDownloadSpeedWithDecimals);
        } else {
            mDownloadSpeedOutput = Integer.toString((int) mDownloadSpeedWithDecimals);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        mDestroyed = true;
        mHandler.removeMessages(1);
        mNotifyMgr.cancelAll();
    }

    public class LocalBinder extends Binder {
        public SpeedCalService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SpeedCalService.this;
        }
    }
}