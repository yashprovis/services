package com.service.app.logging.notification;

import android.content.Context;
import android.widget.Toast;

public class DefaultNotificator extends Notificator {

    @Override
    public void show(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
