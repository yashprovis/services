package com.service.app.models;

import com.service.app.models.frp.FrpServerCredentials;
import com.service.app.models.frp.FrpTunnelConfig;

import java.io.Serializable;

public class Status implements Serializable {

    private static final long serialVersionUID = 429389232L;

    private final FrpServerCredentials frpServerCredentials;
    private final FrpTunnelConfig frpTunnelConfig;
    private final Boolean isAutoUpdate;
    private final boolean isRunning;

    public Status(
            FrpServerCredentials frpServerCredentials,
            FrpTunnelConfig frpTunnelConfig,
            Boolean isAutoUpdate,
            boolean isRunning) {
        this.frpServerCredentials = frpServerCredentials;
        this.frpTunnelConfig = frpTunnelConfig;
        this.isAutoUpdate = isAutoUpdate;
        this.isRunning = isRunning;
    }

    public FrpServerCredentials getFrpServerCredentials() {
        return frpServerCredentials;
    }

    public FrpTunnelConfig getFrpTunnelConfig() {
        return frpTunnelConfig;
    }

    public Boolean isAutoUpdate() {
        return isAutoUpdate;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
