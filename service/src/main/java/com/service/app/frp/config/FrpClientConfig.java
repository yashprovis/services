package com.service.app.frp.config;



import com.service.app.frp.config.parts.CommonPart;
import com.service.app.frp.config.parts.ConfigPart;
import com.service.app.frp.config.parts.TunnelPart;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FrpClientConfig {

    private CommonPart commonPart;
    private final Map<String, TunnelPart> configParts = new HashMap<>();

    public FrpClientConfig() {
    }

    protected FrpClientConfig(FrpClientConfig config) {
        commonPart = config.commonPart != null ? config.commonPart.clone() : null;
        configParts.putAll(config.configParts
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
    }

    public CommonPart getCommonConfig() {
        return commonPart;
    }

    public void setCommonConfig(CommonPart commonConfig) {
        this.commonPart = commonConfig;
    }

    public Map<String, TunnelPart> getTunnelConfigs() {
        return Collections.unmodifiableMap(configParts);
    }

    public TunnelPart getTunnelConfig(String name) {
        return configParts.get(name);
    }

    public void addTunnelConfig(TunnelPart tunnelPart) {
        configParts.put(tunnelPart.getHeader(), tunnelPart);
    }

    public void removeTunnelConfig(String name) {
        TunnelPart part = configParts.get(name);
        if (part != null)
            configParts.remove(part.getHeader());
    }

    public String format() {
        StringBuilder builder = new StringBuilder();
        if (commonPart != null)
            builder.append(commonPart.formatPart());
        configParts.forEach((key, value) -> builder.append("\r\n").append(value.formatPart()));

        return builder.toString();
    }

    public boolean isValid() {
        return commonPart != null &&
                commonPart.isValid() &&
                configParts.size() >= 1 &&
                configParts.values().stream().allMatch(ConfigPart::isValid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FrpClientConfig that = (FrpClientConfig) o;
        return Objects.equals(commonPart, that.commonPart) &&
                Objects.equals(configParts, that.configParts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commonPart, configParts);
    }


    @Override
    public FrpClientConfig clone() {
        return new FrpClientConfig(this);
    }
}
