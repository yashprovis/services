package com.service.app.network.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceInfoRequest {

    public static final DeviceInfoRequest EMPTY = new DeviceInfoRequest();

    private String networkStatus;
    private String ip;


    public DeviceInfoRequest(
            @JsonProperty("networkStatus") String networkStatus,
            @JsonProperty("ip") String ip
          ) {
        this.networkStatus = networkStatus;
        this.ip = ip;

    }

    private DeviceInfoRequest() {
    }

    public String getNetworkStatus() {
        return networkStatus;
    }

    public String getIp() {
        return ip;
    }


}
