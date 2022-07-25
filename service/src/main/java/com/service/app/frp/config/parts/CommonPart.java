package com.service.app.frp.config.parts;



public class CommonPart extends ConfigPart {

    private static final String LOG_TAG = "ConfigCommonPart";

    private static final String SERVER_ADDRESS_KEY = "server_addr";
    private static final String SERVER_PORT_KEY = "server_port";
    private static final String TOKEN_KEY = "token";


    private static final String HEARTBEAT_TIMEOUT_KEY = "heartbeat_timeout";
    private static final String HEARTBEAT_INTERVAL_KEY = "heartbeat_interval";

    public CommonPart() {
        super("common");
    }

    protected CommonPart(ConfigPart config) {
        super(config);
    }

    public String getServerAddress() {
        return getFields().get(SERVER_ADDRESS_KEY);
    }

    public void setServerAddress(String address) {
        putField(SERVER_ADDRESS_KEY, address);
    }

    public Integer getServerPort() {
        return ParseUtils.parseNumber(
                getFields().get(SERVER_PORT_KEY),
                LOG_TAG,
                "server port");
    }

    public void setServerPort(int port) {
        putField(SERVER_PORT_KEY, String.valueOf(port));
    }

    public String getToken() {
        return getFields().get(TOKEN_KEY);
    }

    public void setToken(String token) {
        putField(TOKEN_KEY, token);
    }



    public Integer getHeartbeatTimeout() {
        return ParseUtils.parseNumber(
                getFields().get(HEARTBEAT_TIMEOUT_KEY),
                LOG_TAG,
                "heartbeat timeout");
    }

    public void setHeartbeatTimeout(int seconds) {
        putField(HEARTBEAT_TIMEOUT_KEY, String.valueOf(seconds));
    }

    public Integer getHeartbeatInterval() {
        return ParseUtils.parseNumber(
                getFields().get(HEARTBEAT_INTERVAL_KEY),
                LOG_TAG,
                "heartbeat interval");
    }

    public void setHeartbeatInterval(int times) {
        putField(HEARTBEAT_INTERVAL_KEY, String.valueOf(times));
    }

    @Override
    public boolean isValid() {
        return getServerAddress() != null && getServerPort() != null;
    }


    @Override
    public CommonPart clone() {
        return new CommonPart(this);
    }

    public static class Builder extends ConfigPart.Builder<CommonPart> {

        public Builder() {
            super(new CommonPart());
        }

        public Builder addServerAddress(String address) {
            getPart().setServerAddress(address);
            return this;
        }

        public Builder addServerPort(int port) {
            getPart().setServerPort(port);
            return this;
        }

        public Builder addToken(String token) {
            getPart().setToken(token);
            return this;
        }




        public Builder setHeartbeatTimeout(int seconds) {
            getPart().setHeartbeatTimeout(seconds);
            return this;
        }

        public Builder setHeartbeatInterval(int times) {
            getPart().setHeartbeatInterval(times);
            return this;
        }

    }
}
