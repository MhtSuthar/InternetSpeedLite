package com.internetspeedlite.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.internetspeedlite.utilz.AppUtilz;
import com.internetspeedlite.utilz.Constants;
import com.internetspeedlite.utilz.ObservableObject;


/**
 * Created by Android-132 on 11-Feb-16.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        boolean status = AppUtilz.isOnline(context);
        Intent observIntent = new Intent();
        observIntent.putExtra(Constants.KEY_IS_NET_ON, status);
        ObservableObject.getInstance().updateValue(observIntent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
