package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsEnum;

public enum PrivateNutsRepositorySelectorOp implements NutsEnum {
    INCLUDE,
    EXCLUDE,
    EXACT;
    private final String id;

    PrivateNutsRepositorySelectorOp() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static PrivateNutsRepositorySelectorOp parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static PrivateNutsRepositorySelectorOp parseLenient(String value, PrivateNutsRepositorySelectorOp emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static PrivateNutsRepositorySelectorOp parseLenient(String value, PrivateNutsRepositorySelectorOp emptyValue, PrivateNutsRepositorySelectorOp errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return PrivateNutsRepositorySelectorOp.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    @Override
    public String id() {
        return id;
    }
}
