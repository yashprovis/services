package com.service.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;

public class FrpServerCredentialsResponse implements Serializable {

    private final String name;

//    private final Integer localPort;
//    private final String group;
//    private final String groupKey;
//
//    private final String adminAddr;
//    private final Integer adminPort;
//    private final String adminUser;
// //   private final String adminPass;
//
//    private final String type;
//    private final String localIp;
//    private final String customDomain;
//    private final String healthCheckType;
//    private final Integer healthCheckTimeout;
//    private final Integer healthCheckMaxFailed;
//    private final Integer healthCheckInterval;
private final HashMap comman;
    private final HashMap extras;
    public FrpServerCredentialsResponse(
            String name,
           HashMap comman,
            HashMap extras
    ) {
        this.name=name;
        this.comman=comman;
//        this.group = group;
//        this.groupKey = groupKey;
//        this.localPort = localPort;
//        this.customDomain = customDomain;
//        this.healthCheckInterval=healthCheckInterval;
//        this.healthCheckTimeout=healthCheckTimeout;
//        this.healthCheckMaxFailed=healthCheckMaxFailed;
//        this.healthCheckType=healthCheckType;
//        this.localIp=localIp;
//        this.type=type;
//        this.adminUser=adminUser;
//     //   this.adminPass = adminPass;
//        this.adminPort=adminPort;
//        this.adminAddr =adminAddr;

        this.extras= extras;
    }

//    public String getServerAddress() {
//        return serverAddress;
//    }
    public String getName() {
        return name;
    }

//    public Integer getServerPort() {
//
//        return serverPort;
//    }
    public HashMap<String,String> getExtras() {
        return extras;
    }
    public HashMap<String,String> getComman() {
        return comman;
    }
//    public String getToken() {
//        return token;
//    }

//    public Integer getLocalPort() {
//        return localPort;
//    }
//
//    public String getGroup() {
//        return group;
//    }
//
//    public String getGroupKey() {
//        return groupKey;
//    }
//
//
//    public String getAdminAddr() {
//        return adminAddr;
//    }
//
//    public Integer getAdminPort() {
//        return adminPort;
//    }
//
//    public String getAdminUser() {
//        return adminUser;
//    }
//
////    public String getAdminPass() {
////        return adminPass;
////    }
//
//    public String getLocalIp() {
//        return localIp;
//    }
//
//    public Integer getHealthCheckTimeout() {
//
//        return healthCheckTimeout;
//    }
//
//    public Integer getHealthCheckInterval() {
//        return healthCheckInterval;
//    }
//
//    public Integer getHealthCheckMaxFailed() {
//        return healthCheckMaxFailed;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public String getHealthCheckType() {
//        return healthCheckType;
//    }
//    public String getCustomDomain() {
//        return customDomain;
//    }
}
