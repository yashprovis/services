package com.service.app.network;



import com.service.app.network.utils.RepositoryCallbackWithoutResponse;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FrpAdminRepository {

    private final String LOG_TAG = this.getClass().getName();

    private final String API_URL;
    private final String API_RELOAD_URL;
    private final String API_STOP_URL;

    private final String AUTH_USERNAME;
    private final String AUTH_PASSWORD;
    private final String basicCredentials;
    private final OkHttpClient.Builder builder = new OkHttpClient.Builder();
    private final OkHttpClient client;

    public FrpAdminRepository(String ip, int port, String authUsername, String authPassword) {
        API_URL = "http://" + ip + ":" + port + "/api";
        System.out.println(API_URL);
        API_RELOAD_URL = API_URL + "/reload";
        API_STOP_URL = API_URL + "/stop";
        AUTH_USERNAME = authUsername;
        AUTH_PASSWORD = authPassword;

        basicCredentials = Credentials.basic(AUTH_USERNAME, AUTH_PASSWORD);
        builder.authenticator((route, response) -> response
                .request()
                .newBuilder()
                .header("Authorization", basicCredentials)
                .build());
        client = builder.build();
    }

    public CompletableFuture<Void> reload() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Request request = new Request.Builder().url(API_RELOAD_URL) .header("Authorization", basicCredentials).build();
        client.newCall(request).enqueue(new RepositoryCallbackWithoutResponse(
                LOG_TAG,
                "reload",
                future) {
            @Override
            public void onFailure(Call call, IOException e) {

                    super.onFailure(call, e);
                    future.completeExceptionally(e);

            }
        });

        return future;
    }

    public CompletableFuture<Void> stop() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Request request = new Request.Builder().url(API_STOP_URL).header("Authorization", basicCredentials).build();
        client.newCall(request).enqueue(new RepositoryCallbackWithoutResponse(
                LOG_TAG,
                "stop",
                future) {
            @Override
            public void onFailure(Call call, IOException e) {

                    super.onFailure(call, e);
                    future.completeExceptionally(e);

            }
            @Override
            public  void onResponse(Call call, Response r){
                try {
                    super.onResponse(call,r);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return future;
    }

}
