package com.service.app;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;


import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.app.android_service.Config;
import com.service.app.android_service.ForegroundService;

import com.service.app.logging.log.Logger;
import com.service.app.logging.log.LoggerFactory;
import com.service.app.models.Status;
import com.service.app.models.frp.FrpServerCredentials;
import com.service.app.models.frp.FrpTunnelConfig;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ServiceController implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger();

    private final static String LOG_TAG = ServiceController.class.getName();
    private final static String SHARED_PREFERENCES_NAME =
            ServiceController.class.getName() + ".preferences";

    private final Context context;

    private BroadcastReceiver statusReceiver;
    private Consumer<Status> statusListener;

    ServiceController(Context context) {
        this.context = context;
    }

    public static void requestRequiredPermissions(Activity activity) {
        List<String> permissions = new ArrayList<>(Collections.singletonList(
                Manifest.permission.READ_PHONE_STATE));

        List<String> permissionsToRequest = permissions
                .stream()
                .filter((e) -> ContextCompat.checkSelfPermission(activity, e) !=
                        PackageManager.PERMISSION_GRANTED)
                .collect(Collectors.toList());


        if (permissionsToRequest.size() > 0)
            permissionsToRequest.forEach((e) -> ActivityCompat.requestPermissions(
                    activity,
                    new String[]{e},
                    1));
    }


    public void setStatusListener(Consumer<Status> statusListener) {
        registerStatusReceiver();
        this.statusListener = statusListener;
    }

    public boolean isRunning() {
        ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ForegroundService.class.getName().equals(service.service.getClassName())) {
                logger.i(LOG_TAG, "Service status: running");
                return true;
            }
        }
        logger.i(LOG_TAG, "Service status: not running");
        return false;
    }

    public void stop() {
        if (isRunning())
            context.stopService(new Intent(context, ForegroundService.class));
    }

    @Override
    public void close() {
        if (statusReceiver != null)
            context.unregisterReceiver(statusReceiver);
    }


    void run(Config config) {
        System.out.print("here");
        Intent intent = new Intent(context, ForegroundService.class);
        intent.setPackage(context.getPackageName());

        intent.putExtra(ForegroundService.FRP_CONFIG_EXTRA, config);

        ForegroundService fService = new ForegroundService();
        // fService.startProject(intent);
//        if (!isRunning()) {
//
//
//        } else {
//            Intent intent = new Intent(ForegroundService.BROADCAST_UPDATE_ACTION);
//            intent.putExtra(ForegroundService.FRP_CONFIG_EXTRA, config);
//
//            context.sendBroadcast(intent);
//        }
    }



    private void registerStatusReceiver() {
        statusReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if (extras != null && extras.get(ForegroundService.FRP_STATUS_EXTRA) != null) {
                    Status status = (Status) extras.getSerializable(
                            ForegroundService.FRP_STATUS_EXTRA);

                    if (statusListener != null)
                        statusListener.accept(status);
                }
            }
        };

        IntentFilter intFilter =
                new IntentFilter(ForegroundService.BROADCAST_STATUS_INFO_ACTION);
        context.registerReceiver(statusReceiver, intFilter);
    }

    public static abstract class Builder<T extends Builder> {

        protected final Context context;


        Builder(Context context) {
            this.context = context;
        }

        public static AutomaticBuilder automatic(Context context) {
            return new AutomaticBuilder(context);
        }

        public static ManualBuilder manual(Context context,
                                           FrpServerCredentials frpServerCredentials,
                                           FrpTunnelConfig tunnelConfig) {
            return new ManualBuilder(context, frpServerCredentials, tunnelConfig);
        }

        public ServiceController run() {
            ServiceController serviceController = new ServiceController(context);
            serviceController.run(buildConfig(makeConfig()));

            return serviceController;
        }

        protected abstract Config makeConfig();

        protected Config buildConfig(Config config) {


            return config;
        }


    }

    public static class ManualBuilder extends Builder<ManualBuilder> {

        private FrpServerCredentials frpServerCredentials;
        private FrpTunnelConfig tunnelConfig;

        ManualBuilder(
                Context context,
                FrpServerCredentials frpServerCredentials,
                FrpTunnelConfig tunnelConfig) {
            super(context);
            this.frpServerCredentials = frpServerCredentials;
            this.tunnelConfig = tunnelConfig;
        }

        @Override
        protected Config makeConfig() {
            return Config.manual();
        }

        @Override
        protected Config buildConfig(Config config) {
            config = super.buildConfig(config);
            config.setFrpServerCredentials(frpServerCredentials);
            config.setFrpTunnelConfig(tunnelConfig);

            return config;
        }
    }

    public static class AutomaticBuilder extends Builder<AutomaticBuilder> {

        private boolean isAutoUpdate;

        AutomaticBuilder(Context context) {
            super(context);
            isAutoUpdate = true;
        }

        @Override
        protected Config makeConfig() {
            return Config.automatic();
        }

        @Override
        protected Config buildConfig(Config config) {
            config = super.buildConfig(config);
            config.setAutoUpdate(isAutoUpdate);

            return config;
        }

        public AutomaticBuilder setAutoUpdate(boolean isEnabled) {
            this.isAutoUpdate = isEnabled;
            return this;
        }
    }
}
