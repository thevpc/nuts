package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsParseEnumException;
import net.thevpc.nuts.NutsSession;

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

    public static PrivateNutsRepositorySelectorOp parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static PrivateNutsRepositorySelectorOp parse(String value, PrivateNutsRepositorySelectorOp emptyValue, NutsSession session) {
        PrivateNutsRepositorySelectorOp v = parseLenient(value, emptyValue, null);
        if (v == null) {
            if (!NutsBlankable.isBlank(value)) {
                throw new NutsParseEnumException(session, value, PrivateNutsRepositorySelectorOp.class);
            }
        }
        return v;
    }

    @Override
    public String id() {
        return id;
    }
}
