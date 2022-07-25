package com.service.app.network.utils;



import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.service.app.android_service.Config;
import com.service.app.exceptions.FrpcClientException;
import com.service.app.exceptions.NetworkException;
import com.service.app.frp.config.FrpClientConfig;
import com.service.app.frp.config.parts.CommonPart;
import com.service.app.frp.config.parts.TunnelPart;
import com.service.app.logging.log.Logger;
import com.service.app.network.models.FrpServerCredentialsResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import frpclib.Frpclib;
import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CallbackWithResponseFRC extends RepositoryCallback{

    private CompletableFuture<FrpServerCredentialsResponse> future;
    private CompletableFuture<Void> frpcLibFuture;
    public CallbackWithResponseFRC (
            String logTag,
            String requestName,
            CompletableFuture<FrpServerCredentialsResponse> future
    ) {
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
        ResponseBody body = response.body();
        if (body == null) {
            future.completeExceptionally(
                    new NetworkException(
                            "Request failed with code: " + response.code() +
                                    ". Response body does not exists"));
            return;
        }
        String responseBody = body.string();
        logger.d(LOG_TAG, requestName + ", onResponse: " + responseBody);
        if (response.isSuccessful()) {


            try {
//                JSONObject object = new JSONObject();
                JSONObject obj =new JSONObject(responseBody);
//
                String name= obj.getJSONArray("configuration").getJSONObject(1).getString("section_name");
                name = name.substring(1,name.length()-1);
                JSONArray commanFields=obj.getJSONArray("configuration").getJSONObject(0).getJSONArray("section_fields");
                HashMap comman = new HashMap();
                if(commanFields.length()>0){
                    for (int i=0;i<commanFields.length();i++){
                        comman.put(commanFields.getJSONObject(i).getString("property_name"),commanFields.getJSONObject(i).getString("property_value"))     ;
                    }
                }
//
                JSONArray customFields=obj.getJSONArray("configuration").getJSONObject(1).getJSONArray("section_fields");
                HashMap extras = new HashMap();
                if(customFields.length()>0){
                    for (int i=0;i<customFields.length();i++){
                 extras.put(customFields.getJSONObject(i).getString("property_name"),customFields.getJSONObject(i).getString("property_value"))     ;
                    }
                }
//

             future.complete(new   FrpServerCredentialsResponse( name,comman,extras));


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

//
}