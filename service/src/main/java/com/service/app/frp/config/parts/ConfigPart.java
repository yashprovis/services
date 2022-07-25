package com.service.app.frp.config.parts;



import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigPart {

    private final String header;
    private final Map<String, String> fields = new HashMap<>();

    public ConfigPart(String header) {
        this.header = header;
    }

    protected ConfigPart(ConfigPart config) {
        header = config.header;
        fields.putAll(config.fields);
    }

    public String getHeader() {
        return header;
    }

    public Map<String, String> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    public void putField(String key, String value) {
        fields.put(key, value);
    }

    public void removeField(String key) {
        fields.remove(key);
    }

    public String formatPart() {
        StringBuilder builder = new StringBuilder("[" + header + "]\r\n");
        fields.forEach((key, value) -> builder.append(key).append("=").append(value).append("\r\n"));

        return builder.toString();
    }

    public boolean isValid() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigPart that = (ConfigPart) o;
        return Objects.equals(header, that.header) &&
                Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, fields);
    }

    @NonNull
    @Override
    public ConfigPart clone() {
        return new ConfigPart(this);
    }

    public static class Builder<T extends ConfigPart> {

        private final T part;

        public Builder(T part) {
            this.part = part;
        }

        public T build() {
            return part;
        }

        protected T getPart() {
            return part;
        }

    }

}
