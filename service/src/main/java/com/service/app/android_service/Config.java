package com.service.app.android_service;

import com.service.app.models.frp.FrpServerCredentials;
import com.service.app.models.frp.FrpTunnelConfig;

import java.io.Serializable;

public class Config implements Serializable {

    private static final long serialVersionUID = 429389232L;

    private FrpServerCredentials frpServerCredentials;
    private FrpTunnelConfig frpTunnelConfig;
    private Boolean isAutoUpdate;
    private Type type;
    private NotificationConfig notificationConfig;

    private Config(Type type) {
        this.type = type;
    }

    public static Config automatic() {
        return new Config(Type.AUTO);
    }

    public static Config manual() {
        return new Config(Type.MANUAL);
    }

    public void setFrpServerCredentials(FrpServerCredentials frpServerCredentials) {
        this.frpServerCredentials = frpServerCredentials;
    }

    public void setFrpTunnelConfig(FrpTunnelConfig frpTunnelConfig) {
        this.frpTunnelConfig = frpTunnelConfig;
    }

    public void setAutoUpdate(Boolean autoUpdate) {
        isAutoUpdate = autoUpdate;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setNotificationConfig(NotificationConfig notificationConfig) {
        this.notificationConfig = notificationConfig;
    }

    public FrpServerCredentials getFrpServerCredentials() {
        return frpServerCredentials;
    }

    public FrpTunnelConfig getFrpTunnelConfig() {
        return frpTunnelConfig;
    }

    public Boolean getAutoUpdate() {
        return isAutoUpdate;
    }

    public Type getType() {
        return type;
    }

    public NotificationConfig getNotificationConfig() {
        return notificationConfig;
    }

    public enum Type {
        AUTO, MANUAL
    }
}
