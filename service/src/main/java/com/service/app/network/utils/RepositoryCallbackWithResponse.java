package com.service.app.network.utils;



import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.app.exceptions.NetworkException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RepositoryCallbackWithResponse<T> extends RepositoryCallback {

    private CompletableFuture<T> future;
    private Class<T> parseClassName;

    public RepositoryCallbackWithResponse(
            String logTag,
            String requestName,
            CompletableFuture<T> future,
            Class<T> parseClassName) {
        super(logTag, requestName);
        this.future = future;
        this.parseClassName = parseClassName;
    }

    @Override
    public void onFailure(@NonNull Call call, IOException e) {
        super.onFailure(call, e);
        future.completeExceptionally(e);
    }

    @Override
    public void onResponse(@NonNull Call call, Response response) throws IOException {
        ResponseBody body = response.body();
        if (body == null) {
            future.completeExceptionally(
                    new NetworkException(
                            "Request failed with code: " + response.code() +
                                    ". Response body does not exists"));
            return;
        }
        String responseBody = body.string();

        if (response.isSuccessful()) {
            logger.d(LOG_TAG, requestName + ", onResponse: " + responseBody);

            try {
                ObjectMapper mapper = new ObjectMapper();

                T result = mapper.readValue(
                        responseBody,
                        parseClassName);

                future.complete(result);
            } catch (Exception exc) {
                future.completeExceptionally(exc);
            }
        } else {
            logger.e(LOG_TAG, requestName +
                    ", onResponseNotSuccessful: Code: " + response.code() +
                    ", " + responseBody);

            future.completeExceptionally(
                    new NetworkException("Request failed with code: " + response.code() +
                            ". Content: " + responseBody));
        }
    }
}
