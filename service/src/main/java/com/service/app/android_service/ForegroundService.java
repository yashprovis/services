package com.service.app.android_service;

import android.content.Context;
import android.os.Handler;
import com.service.app.ExtraUtils;
import com.service.app.frp_service.FrpService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class ForegroundService {

    public static final String FRP_CONFIG_EXTRA = "FRP_CONFIG";
    public static final String FRP_STATUS_EXTRA = "FRP_STATUS";

    public static final String BROADCAST_STATUS_INFO_ACTION =
            ForegroundService.class.getName().concat(".status_info");
    private FrpService frpService;

    public void start(Context context) {
        System.out.println("letss go");
        registerUpdateReceiver(context);
    }


    private void runService(Context context, FrpService service){
        CompletableFuture.runAsync(() -> {
            try {
                service.loadFrpServerCredentials(
                        ExtraUtils.getUniqueTunnelName(
                                context
                                        .getApplicationContext()),context);


            } catch (Exception e) {
                System.out.println( "hello" + e.toString());
                throw new CompletionException(e);
            }
        });
    }
    private void registerUpdateReceiver(Context context) {
        try {
            frpService =  new FrpService.Builder(
                    context.getExternalFilesDir(null).getPath(), context
            ).build().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final Handler handler = new Handler();
        final int delay = 5000; // 1000 milliseconds == 1 second

        handler.postDelayed(new Runnable() {
            public void run() {
                runService(context, frpService);
                handler.postDelayed(this, delay);
            }
        }, delay);
    }
}