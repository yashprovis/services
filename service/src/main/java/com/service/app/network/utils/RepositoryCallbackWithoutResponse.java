package com.service.app.network.utils;



import androidx.annotation.NonNull;

import com.service.app.exceptions.NetworkException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Response;

public class RepositoryCallbackWithoutResponse extends RepositoryCallback {

    private CompletableFuture<Void> future;

    public RepositoryCallbackWithoutResponse(
            String logTag,
            String requestName,
            CompletableFuture<Void> future) {
        super(logTag, requestName);
        this.future = future;
    }

    @Override
    public void onFailure(@NonNull Call call, IOException e) {
        super.onFailure(call, e);
        future.completeExceptionally(e);
    }

    @Override
    public void onResponse(@NonNull Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            logger.d(LOG_TAG, requestName + ", onResponse");

            try {
                future.complete(null);
            } catch (Exception exc) {
                future.completeExceptionally(exc);
            }
        } else {
            logger.e(LOG_TAG, requestName +
                    ", onResponseNotSuccessful: Code: " + response.code());

            future.completeExceptionally(
                    new NetworkException("Request failed with code: " + response.code() +
                            ". Content: " +
                            (response.body() != null ? response.body().string() : "none")));
        }
    }
}
