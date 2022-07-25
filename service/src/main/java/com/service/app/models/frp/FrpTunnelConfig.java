package com.service.app.models.frp;

import java.io.Serializable;
import java.util.HashMap;

public class FrpTunnelConfig implements Serializable {

    private String tunnelName;
    private Integer remotePort;
    private String localIp = "127.0.0.1";
    private Integer localPort;
    private String group;
    private String groupKey;

    private String adminUser = "0.0.0.0";
    private Integer adminPort;
    private String adminPswd;
    private String adminAddr;

    private String customDomain;
    private String healthType;
    private Integer healthTimeout;
    private Integer healthInterval;
    private Integer healthFailed;
    private HashMap extraFields;

    public FrpTunnelConfig(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public String getTunnelName() {
        return tunnelName;
    }

    public void setTunnelName(String tunnelName) {
        this.tunnelName = tunnelName;
    }

    public HashMap<String,String> getextras() {
        return extraFields;
    }

    public void setExtras(HashMap extras) {
        this.extraFields = extras;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getCustomDomain() {
        return customDomain;
    }

    public void setCustomDomain(String customDomain) {
        this.customDomain = customDomain;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }
    /////
    public String getAdminPswd() {
        return adminPswd;
    }

    public void setAdminPswd(String pswd) {
        this.adminPswd = pswd;
    }

    public Integer getAdminPort() {
        return adminPort;
    }

    public void setAdminPort(int adminPort) {
        this.adminPort = adminPort;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getAdminAddr() {
        return adminAddr;
    }

    public void setAdminAddr(String adminAddr) {
        this.adminAddr = adminAddr;
    }

    ///
    public Integer getHealthFailed() {
        return healthFailed;
    }

    public void setHealthFailed(int healthFailed) {
        this.healthFailed = healthFailed;
    }

    public String getHealthType() {
        return healthType;
    }

    public void setHealthType(String healthType) {
        this.healthType = healthType;
    }

    public Integer getHealthTimeout() {
        return healthTimeout;
    }

    public void setHealthTimeout(int healthTimeout) {
        this.healthTimeout = healthTimeout;
    }

    public Integer getHealthInterval() {
        return healthInterval;
    }

    public void setHealthInterval(int healthInterval) {
        this.healthInterval = healthInterval;
    }
}
