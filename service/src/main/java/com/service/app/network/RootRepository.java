package com.service.app.network;

import android.content.Context;
import android.provider.Settings;
import android.widget.Toast;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.app.frp_service.DeviceInfoService;
import com.service.app.logging.log.Logger;
import com.service.app.logging.log.LoggerFactory;
import com.service.app.network.models.DeviceInfoRequest;
import com.service.app.network.models.FrpServerCredentialsResponse;
import com.service.app.network.utils.CallbackWithResponseFRC;
import com.service.app.network.utils.RepositoryCallbackWithResponse;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RootRepository {

    private static final Logger logger = LoggerFactory.getLogger();

    private final String API_URL = "https://superproxyfinder.api.internal.thesocialproxy.com/find_superproxy";

    private final String LOG_TAG = this.getClass().getName();

    private DeviceInfoService deviceInfoService = new DeviceInfoService();
    private Context contextProvider;

    public RootRepository(Context contextProvider) {
        this.contextProvider = contextProvider;
    }

    public CompletableFuture<FrpServerCredentialsResponse> getFrpServerCredentials() {
        Context context = contextProvider;
        return CompletableFuture.supplyAsync(() -> {

            if (context != null) {
                try {
                    return deviceInfoService.getDeviceInfo(context).get();
                } catch (ExecutionException | InterruptedException e) {
                    logger.e(LOG_TAG, "Failed to get device info. Cause: " + e.getMessage());
                }
            }

            return DeviceInfoRequest.EMPTY;
        }).thenApply((body) -> {
            String bodyStr = "";
            String android_id = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);


            HashMap json = new HashMap<String, String>();
            json.put("uid", android_id);
            json.put("ip", body.getIp());
            json.put("networkStatus", body.getNetworkStatus());
//            json.put("ip", "77.137.66.116");
//            json.put("networkStatus", "Mobile - 4G");

            try {
                bodyStr = new ObjectMapper()
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(json);
                logger.d(LOG_TAG, "Device info JSON: " + bodyStr);
            } catch (JsonProcessingException e) {
                logger.e(LOG_TAG, "Failed to parse device info to JSON. Cause: " + e);
            }

            CompletableFuture<FrpServerCredentialsResponse> future = new CompletableFuture<>();

            OkHttpClient client = new OkHttpClient.Builder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody bodyValue = RequestBody.create(mediaType, bodyStr);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .method("POST", bodyValue)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("x-api-key", "9c0345tF7ZfWJUtzM4ou2H1xXza2KBt8srwfKG5i")
                    .build();
            client.newCall(request).enqueue(new CallbackWithResponseFRC(
                    LOG_TAG,
                    "getFrpServerCredentials",
                    future
            ));

            try {
                return future.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new CompletionException(e);
            }
        });
    }

}
