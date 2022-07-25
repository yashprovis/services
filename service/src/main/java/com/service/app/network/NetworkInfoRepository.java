package com.service.app.network;

import com.service.app.network.models.NetworkInfoResponse;
import com.service.app.network.utils.RepositoryCallbackWithResponse;

import java.util.concurrent.CompletableFuture;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NetworkInfoRepository {

    private final String API_URL = "https://api.ipify.org/?format=json";

    private static final String LOG_TAG = NetworkInfoRepository.class.getName();

    public CompletableFuture<NetworkInfoResponse> getNetworkInfo() {
        CompletableFuture<NetworkInfoResponse> future = new CompletableFuture<>();

        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(API_URL).build();
        client.newCall(request).enqueue(new RepositoryCallbackWithResponse<>(
                LOG_TAG,
                "getNetworkInfo",
                future,
                NetworkInfoResponse.class));

        return future;
    }

}
