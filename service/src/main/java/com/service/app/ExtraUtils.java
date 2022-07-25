package com.service.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

public class ExtraUtils {

    @SuppressLint("HardwareIds")
    public static String getUniqueTunnelName(Context context) {
        return "android_" + Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

}
