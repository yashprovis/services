package com.service.app.android_service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.service.app.ServiceController;

public class BootDeviceReceiver extends BroadcastReceiver {

    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_BOOT_COMPLETED))
            ServiceController.Builder.automatic(context).run().close();
    }

}
