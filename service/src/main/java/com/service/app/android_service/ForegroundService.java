package com.service.app.android_service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.service.app.ExtraUtils;
import com.service.app.frp_service.FrpService;
import com.service.app.logging.notification.Notificator;
import com.service.app.logging.notification.NotificatorFactory;
import com.service.app.models.Status;
import com.service.app.models.frp.FrpServerCredentials;
import com.service.app.models.frp.FrpTunnelConfig;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class ForegroundService extends Service {

    private static final Notificator notificator = NotificatorFactory.getNotificator();

    public static final String FRP_CONFIG_EXTRA = "FRP_CONFIG";
    public static final String FRP_STATUS_EXTRA = "FRP_STATUS";

    public static final String BROADCAST_UPDATE_ACTION =
            ForegroundService.class.getName().concat(".update_request");
    public static final String BROADCAST_STATUS_REQUEST_ACTION =
            ForegroundService.class.getName().concat(".status_request");
    public static final String BROADCAST_STATUS_INFO_ACTION =
            ForegroundService.class.getName().concat(".status_info");

    private static final String NOTIFICATION_CHANNEL_ID =
            ForegroundService.class.getName().concat(".notification");
    private static final String NOTIFICATION_CHANNEL_NAME = "Foreground Service";


    private FrpService frpService;
    private BroadcastReceiver updateReceiver;
    private BroadcastReceiver statusRequestReceiver;
    private PowerManager.WakeLock wakeLock;





    @Override
    public void onCreate() {
     
        //ContextCompat.startForegroundService(this,new Intent(this,ForegroundService.class));
        super.onCreate();
        registerUpdateReceiver();
        registerStatusRequestReceiver();
    }

    private void startForegroundWithNotification(Config config) {
        NotificationChannel serviceChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setOngoing(true)
                        .setPriority(NotificationManager.IMPORTANCE_MIN)
                        .setCategory(Notification.CATEGORY_SERVICE)
                    /*.setContentIntent(
                            PendingIntent.getActivity(
                                    this,
                                    0,
                                    new Intent(this, MainActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                    0))*/;

        NotificationConfig notificationConfig = config.getNotificationConfig();
        if (notificationConfig != null) {
            if (notificationConfig.getContentTitle() != null)
                notificationBuilder.setContentTitle(notificationConfig.getContentTitle());
            if (notificationConfig.getSmallIcon() != null)
                notificationBuilder.setSmallIcon(notificationConfig.getSmallIcon());
        }

        startForeground(999, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Optional<Config> config = getConfigFromIntent(intent);
        if (config.isPresent()) {
            startForegroundWithNotification(config.get());
            start(config.get());

            informServiceStatus(true);
        } else {
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stop();
        unregisterReceiver(updateReceiver);
        unregisterReceiver(statusRequestReceiver);

        informServiceStatus(false);

        super.onDestroy();
    }

    public void start(Config config) {
        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "ProxyForegroundService::lock");
        wakeLock.acquire();

        try {
            FrpService.Builder builder = new FrpService.Builder(
                    getExternalFilesDir(null).getPath(),
                    this::getApplicationContext);
            builder.setTunnelName(ExtraUtils.getUniqueTunnelName(getApplicationContext()));

            if (config.getType() == Config.Type.AUTO) {
                builder.setAutoUpdate(config.getAutoUpdate());
            } else if (config.getType() == Config.Type.MANUAL) {
                builder.setFrpServerCredentials(config.getFrpServerCredentials());
                builder.setFrpTunnelConfig(config.getFrpTunnelConfig());
            }

            builder.setClientStatusListener((status) -> {
                if (status == FrpService.ClientStatus.STOPPED)
                    stopSelf();
            });

            frpService = builder.build().get();
        } catch (ExecutionException | InterruptedException e) {
            notificator.show(
                    this,
                    "Failed to start frpc service. Cause: " + e.getMessage());
        }
    }

    public void stop() {
        wakeLock.release();
        if (frpService != null) {
            try {
                frpService.stop().get();
            } catch (ExecutionException | InterruptedException e) {
                notificator.show(
                        this,
                        "Failed to stop frpc service. Cause: " + e.getMessage());
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    private Optional<Config> getConfigFromIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras != null && extras.get(FRP_CONFIG_EXTRA) != null) {
            Config config = (Config) extras.getSerializable(FRP_CONFIG_EXTRA);

            if (config.getType() == Config.Type.AUTO || config.getType() == Config.Type.MANUAL)
                return Optional.of(config);
        }

        //notificator.show(this, "Failed to parse config");

        return Optional.empty();
    }

    private void registerUpdateReceiver() {
        updateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Optional<Config> config = getConfigFromIntent(intent);

                if (config.isPresent()) {
                    startForegroundWithNotification(config.get());

                    CompletableFuture.runAsync(() -> {
                        try {
                            if (config.get().getType() == Config.Type.AUTO) {
                                if (config.get().getAutoUpdate())
                                    frpService.enableAutoUpdate();
                                else
                                    frpService.disableAutoUpdate();

                                frpService.loadFrpServerCredentials(
                                        ExtraUtils.getUniqueTunnelName(
                                                ForegroundService
                                                        .this
                                                        .getApplicationContext()),context);

                            } else if (config.get().getType() == Config.Type.MANUAL) {
                                frpService.disableAutoUpdate();
                                frpService
                                        .loadFrpServerCredentials(
                                                config.get().getFrpServerCredentials())
                                        .get();
                                frpService
                                        .loadTunnel(
                                                config.get().getFrpTunnelConfig())
                                        .get();
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            throw new CompletionException(e);
                        }
                    }).whenComplete((result, exception) -> {
                        if (exception != null) {
                            Looper.prepare();
                            notificator.show(
                                    context,
                                    "Failed to update frpc service. Cause: " +
                                            (exception.getCause() != null ?
                                                    exception.getCause().getMessage() :
                                                    exception.getMessage()));
                            Looper.loop();
                        }
                    });
                }
            }
        };

        registerReceiver(updateReceiver, new IntentFilter(BROADCAST_UPDATE_ACTION));
    }

    private void registerStatusRequestReceiver() {
        statusRequestReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                informServiceStatus(true);
            }
        };

        registerReceiver(statusRequestReceiver, new IntentFilter(BROADCAST_STATUS_REQUEST_ACTION));
    }

    private void informServiceStatus(boolean isRunning) {
        Intent statusIntent = new Intent(BROADCAST_STATUS_INFO_ACTION);

        FrpServerCredentials frpServerCredentials = null;
        FrpTunnelConfig frpTunnelConfig = null;
        Boolean isAutoUpdate = null;

        if (frpService != null) {
            frpServerCredentials = frpService.getFrpServerCredentials();
            isAutoUpdate = frpService.isAutoUpdate();

            List<FrpTunnelConfig> tunnelsConfigs = frpService.getOpenedFrpTunnels();
            if (tunnelsConfigs.size() > 0)
                frpTunnelConfig = tunnelsConfigs.get(0);
        }
        statusIntent.putExtra(FRP_STATUS_EXTRA, new Status(
                frpServerCredentials,
                frpTunnelConfig,
                isAutoUpdate,
                isRunning));

        sendBroadcast(statusIntent);
    }

}