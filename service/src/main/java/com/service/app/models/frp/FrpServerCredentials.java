package com.service.app.models.frp;

import java.io.Serializable;

public class FrpServerCredentials implements Serializable {

    private String serverAddress;
    private Integer serverPort;
    private String token;

    public FrpServerCredentials(String serverAddress, int serverPort, String token) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.token = token;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public String getToken() {
        return token;
    }
}
