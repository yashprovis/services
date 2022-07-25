package com.service.app.frp_service;

import android.content.Context;

import com.service.app.network.NetworkInfoRepository;
import com.service.app.network.NetworkUtils;
import com.service.app.network.models.DeviceInfoRequest;
import com.service.app.network.models.NetworkInfoResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class DeviceInfoService {

    private NetworkInfoRepository networkInfoRepository = new NetworkInfoRepository();

    public CompletableFuture<DeviceInfoRequest> getDeviceInfo(Context context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String networkType = NetworkUtils.getNetworkType(context).getName();
                NetworkInfoResponse response = networkInfoRepository.getNetworkInfo().get();

                return new DeviceInfoRequest(
                        networkType,
                        response.getQuery()
                        );
            } catch (ExecutionException | InterruptedException e) {
                throw new CompletionException(e);
            }
        });
    }

}
