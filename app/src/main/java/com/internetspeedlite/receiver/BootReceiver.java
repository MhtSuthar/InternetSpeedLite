package com.internetspeedlite.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.internetspeedlite.service.SpeedCalService;

/**
 * Created by ubuntu on 6/7/16.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mService = new Intent(context, SpeedCalService.class);
        context.startService(mService);
    }
}
