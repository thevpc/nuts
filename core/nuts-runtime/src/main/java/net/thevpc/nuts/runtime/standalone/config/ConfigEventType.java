package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.NutsEnum;

public enum ConfigEventType  implements NutsEnum {
    API, RUNTIME, BOOT, MAIN, SECURITY;
    private String id;

    ConfigEventType() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    @Override
    public String id() {
        return id;
    }

    public static ConfigEventType parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static ConfigEventType parseLenient(String value, ConfigEventType emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static ConfigEventType parseLenient(String value, ConfigEventType emptyValue, ConfigEventType errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return ConfigEventType.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }
}
