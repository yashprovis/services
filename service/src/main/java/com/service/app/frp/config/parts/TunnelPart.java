package com.service.app.frp.config.parts;



import com.service.app.logging.log.Logger;
import com.service.app.logging.log.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TunnelPart extends ConfigPart {

    private static final String LOG_TAG = "ConfigTunnelPart";
    private static final Logger logger = LoggerFactory.getLogger();

    private static final String LOG_FILE="log_file";
    private static final String TYPE_KEY = "type";
    private static final String LOCAL_IP_KEY = "local_ip";
    private static final String LOCAL_PORT_KEY = "local_port";
    private static final String REMOTE_PORT_KEY = "remote_port";
    private static final String GROUP_KEY = "group";
    private static final String GROUP_KEY_KEY = "group_key";
    private static final String HEALTH_CHECK_TYPE_KEY = "health_check_type";
    private static final String HEALTH_CHECK_URL_KEY = "health_check_url";
    private static final String HEALTH_CHECK_TIMEOUT_S_KEY = "health_check_timeout_s";
    private static final String HEALTH_CHECK_MAX_FAILED_KEY = "health_check_max_failed";
    private static final String HEALTH_CHECK_INTERVAL_S_KEY = "health_check_interval_s";
    private static final String BANDWIDTH_LIMIT_KEY = "bandwidth_limit";
    private static final String ADMIN_ADDRESS_KEY = "admin_addr";
    private static final String ADMIN_PORT_KEY = "admin_port";
    private static final String ADMIN_USER_KEY = "admin_user";
    private static final String ADMIN_PASSWORD_KEY = "admin_pwd";

    private static final String USE_ENCRYPTION_KEY = "use_encryption";
    private static final String USE_COMPRESSION_KEY = "use_compression";
    private static final String CUSTOM_DOMAIN = "custom_domains";
    private static final String NAME = "name";
    public TunnelPart(String name, TunnelType tunnelType) {
        super(name);
        setType(tunnelType);
    }

    public String getName() {
        return getFields().get(NAME);
    }

    public void setName(String cD) {
        putField(NAME, cD);
    }

    public String getCustomDomain() {
        return getFields().get(CUSTOM_DOMAIN);
    }

    public void setCustomDomain(String cD) {
        putField(CUSTOM_DOMAIN, cD);
    }

    public String getLogFile() {
        return getFields().get(LOG_FILE);
    }

    public void setLogFile(String cD) {
        putField(LOG_FILE, cD);
    }

    protected TunnelPart(ConfigPart config) {
        super(config);
    }

    public TunnelType getType() {
        return TunnelType.valueOfConfigName(getFields().get(TYPE_KEY));
    }

    public void setType(TunnelType type) {
        putField(TYPE_KEY, type.configName);
    }

    public String getLocalIp() {
        return getFields().get(LOCAL_IP_KEY);
    }

    public void setLocalIp(String ip) {
        putField(LOCAL_IP_KEY, ip);
    }

    public Integer getLocalPort() {
        return ParseUtils.parseNumber(
                getFields().get(LOCAL_PORT_KEY),
                LOG_TAG,
                "local port");
    }

    public void setLocalPort(int port) {
        putField(LOCAL_PORT_KEY, String.valueOf(port));
    }

    public Integer getRemotePort() {
        return ParseUtils.parseNumber(
                getFields().get(REMOTE_PORT_KEY),
                LOG_TAG,
                "remote port");
    }

    public void setRemotePort(int port) {
        putField(REMOTE_PORT_KEY, String.valueOf(port));
    }

    public String getGroup() {
        return getFields().get(GROUP_KEY);
    }

    public void setGroup(String group) {
        putField(GROUP_KEY, group);
    }

    public String getGroupKey() {
        return getFields().get(GROUP_KEY_KEY);
    }

    public void setGroupKey(String groupKey) {
        putField(GROUP_KEY_KEY, groupKey);
    }

    public String getHealthType() {
        return getFields().get(HEALTH_CHECK_TYPE_KEY);
    }

    public void setHealthType(String data) {
        putField(HEALTH_CHECK_TYPE_KEY, data);
    }

    public String getAdminAddress() {
        return getFields().get(ADMIN_ADDRESS_KEY);
    }

    public void setAdminAddress(String address) {
        putField(ADMIN_ADDRESS_KEY, address);
    }

    public Integer getAdminPort() {
        return ParseUtils.parseNumber(
                getFields().get(ADMIN_PORT_KEY),
                LOG_TAG,
                "admin port");
    }

    public void setAdminPort(int port) {
        putField(ADMIN_PORT_KEY, String.valueOf(port));
    }

    public String getAdminUser() {
        return getFields().get(ADMIN_USER_KEY);
    }

    public void setAdminUser(String user) {
        putField(ADMIN_USER_KEY, user);
    }

    public String getAdminPassword() {
        return getFields().get(ADMIN_PASSWORD_KEY);
    }

    public void setAdminPassword(String password) {
        putField(ADMIN_PASSWORD_KEY, password);
    }


    public Integer getHealthCheckTimeoutS() {
        return ParseUtils.parseNumber(
                getFields().get(HEALTH_CHECK_TIMEOUT_S_KEY),
                LOG_TAG,
                "health check timeout s");
    }

    public void setHealthCheckTimeoutS(int seconds) {
        putField(HEALTH_CHECK_TIMEOUT_S_KEY, String.valueOf(seconds));
    }

    public Integer getHealthCheckMaxFailed() {
        return ParseUtils.parseNumber(
                getFields().get(HEALTH_CHECK_MAX_FAILED_KEY),
                LOG_TAG,
                "check max failed");
    }

    public void setHealthCheckMaxFailed(int times) {
        putField(HEALTH_CHECK_MAX_FAILED_KEY, String.valueOf(times));
    }

    public BandwidthLimit getBandwidthLimit() {
        String field = getFields().get(BANDWIDTH_LIMIT_KEY);
        if (field == null)
            return null;
        Matcher matcher = Pattern.compile("[0-9]+(KB|MB)").matcher(field);

        if (matcher.groupCount() < 2) {
            logger.e(
                    LOG_TAG,
                    "Failed to parse bandwidth_limit. Cause: Incorrect format: " + field);
            return null;
        }

        return new BandwidthLimit(ParseUtils.parseNumber(
                matcher.group(0),
                LOG_TAG,
                "bandwidth limit"),
                matcher.group(1).equals("KB") ?
                        BandwidthLimitType.KILOBYTES :
                        BandwidthLimitType.MEGABYTES);
    }

    public void setBandwidthLimit(BandwidthLimit bandwidthLimit) {
        putField(
                BANDWIDTH_LIMIT_KEY,
                bandwidthLimit.value + bandwidthLimit.type.configStr);
    }

    public Integer getHealthCheckIntervalS() {
        return ParseUtils.parseNumber(
                getFields().get(HEALTH_CHECK_INTERVAL_S_KEY),
                LOG_TAG,
                "check interval s");
    }

    public void setHealthCheckIntervalS(int seconds) {
        putField(HEALTH_CHECK_INTERVAL_S_KEY, String.valueOf(seconds));
    }

    public void addField(String key,String value){
        putField(key,value);
    }

    public Boolean isUseEncryption() {
        return ParseUtils.parseBoolean(
                getFields().get(USE_ENCRYPTION_KEY),
                LOG_TAG,
                "is use encryption");
    }

    public void setUseEncryption(boolean value) {
        putField(USE_ENCRYPTION_KEY, String.valueOf(value));
    }

    public Boolean isUseCompression() {
        return ParseUtils.parseBoolean(
                getFields().get(USE_ENCRYPTION_KEY),
                LOG_TAG,
                "is use compression");
    }

    public void setUseCompression(boolean value) {
        putField(USE_COMPRESSION_KEY, String.valueOf(value));
    }

    @Override
    public boolean isValid() {
        return getType() != null;
    }


    @Override
    public TunnelPart clone() {
        return new TunnelPart(this);
    }

    public enum TunnelType {
        TCP("tcp"), UDP("udp"), HTTPS("http");

        private final String configName;

        TunnelType(String configName) {
            this.configName = configName;
        }

        static TunnelType valueOfConfigName(String configName) {
            if (configName == null)
                return null;
            else {
                return List.of(TunnelType.values())
                        .stream()
                        .filter((tunnelType -> tunnelType.configName.equals(configName)))
                        .findFirst()
                        .orElseGet(() -> {
                            logger.e(LOG_TAG, "Unknown TunnelType name: " + configName);
                            return null;
                        });
            }
        }

        String getConfigName() {
            return configName;
        }
    }

    public enum HealthCheckType {
        TCP("tcp"), HTTP("http");

        private final String configName;

        HealthCheckType(String configName) {
            this.configName = configName;
        }

        static HealthCheckType valueOfConfigName(String configName) {
            if (configName == null)
                return null;
            else {
                return List.of(HealthCheckType.values())
                        .stream()
                        .filter((tunnelType -> tunnelType.configName.equals(configName)))
                        .findFirst()
                        .orElseGet(() -> {
                            logger.e(LOG_TAG, "Unknown HealthCheckType name: " + configName);
                            return null;
                        });
            }
        }

        String getConfigName() {
            return configName;
        }
    }

    public static class Builder extends ConfigPart.Builder<TunnelPart> {

        public Builder(String name, TunnelType type) {
            super(new TunnelPart(name, type));
        }

        public Builder addLocalIp(String address) {
            getPart().setLocalIp(address);
            return this;
        }

        public Builder addLocalPort(int port) {
            getPart().setLocalPort(port);
            return this;
        }

        public Builder addRemotePort(int port) {
            getPart().setRemotePort(port);
            return this;
        }

        public Builder addGroup(String group) {
            getPart().setGroup(group);
            return this;
        }

        public Builder addGroupKey(String groupKey) {
            getPart().setGroupKey(groupKey);
            return this;
        }

        public Builder addHealthCheckType(String type) {
            getPart().setHealthType(type);
            return this;
        }



        public Builder addHealthCheckTimeoutS(int seconds) {
            getPart().setHealthCheckTimeoutS(seconds);
            return this;
        }

        public Builder addHealthCheckMaxFailed(int times) {
            getPart().setHealthCheckMaxFailed(times);
            return this;
        }

        public Builder addHealthCheckIntervalS(int seconds) {
            getPart().setHealthCheckIntervalS(seconds);
            return this;
        }

        public Builder addBandwidthLimit(BandwidthLimit bandwidthLimit) {
            getPart().setBandwidthLimit(bandwidthLimit);
            return this;
        }

        public Builder addUseEncryption(boolean value) {
            getPart().setUseEncryption(value);
            return this;
        }

        public Builder addUseCompression(boolean value) {
            getPart().setUseCompression(value);
            return this;
        }

    }

    public static class BandwidthLimit {
        private final BandwidthLimitType type;
        private final int value;

        public BandwidthLimit(int value, BandwidthLimitType type) {
            this.value = value;
            this.type = type;
        }
    }

    public enum BandwidthLimitType {
        KILOBYTES("KB"), MEGABYTES("MB");

        private String configStr;

        BandwidthLimitType(String configStr) {
            this.configStr = configStr;
        }
    }
}
