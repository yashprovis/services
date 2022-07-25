package com.service.app.utils;


import com.service.app.models.frp.FrpServerCredentials;
import com.service.app.models.frp.FrpTunnelConfig;
import com.service.app.network.models.FrpServerCredentialsResponse;

import java.util.ArrayList;

public class Mapper {

    public FrpServerCredentials fromNetworkToFrpServerCredentials(
            FrpServerCredentialsResponse response) {
        return new FrpServerCredentials(
                response.getComman().get("server_addr"),
                Integer.parseInt(response.getComman().get("server_port")),
                response.getComman().get("token"));
    }

    public FrpTunnelConfig fromNetworkToFrpTunnelConfig(FrpServerCredentialsResponse response) {
        FrpTunnelConfig config = new FrpTunnelConfig(
                response.getExtras().get("local_port") != null ? Integer.parseInt(response.getExtras().get("local_port")) : 6000); //TODO: remove hardcoded port
//        config.setTunnelName(response.getName());
//        config.setGroup(response.getGroup());
//        config.setGroupKey(response.getGroupKey());
//        config.setHealthType(response.getHealthCheckType());
//        config.setHealthFailed(response.getHealthCheckMaxFailed());
//        config.setHealthInterval(response.getHealthCheckInterval());
//        config.setHealthTimeout(response.getHealthCheckTimeout());
//        config.setCustomDomain(response.getCustomDomain());
        config.setExtras(response.getExtras());
        return config;
    }

}
