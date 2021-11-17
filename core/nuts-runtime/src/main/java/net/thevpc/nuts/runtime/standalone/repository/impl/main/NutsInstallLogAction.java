package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsParseEnumException;
import net.thevpc.nuts.NutsSession;

public enum NutsInstallLogAction implements NutsEnum {
    INSTALL,
    UNINSTALL,
    REQUIRE,
    UNREQUIRE,
    DEPLOY,
    UNDEPLOY;
    private final String id;

    NutsInstallLogAction() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsInstallLogAction parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsInstallLogAction parse(String value, NutsInstallLogAction emptyValue, NutsSession session) {
        NutsInstallLogAction v = parseLenient(value, emptyValue, null);
        if (v == null) {
            if (!NutsBlankable.isBlank(value)) {
                throw new NutsParseEnumException(session, value, NutsInstallLogAction.class);
            }
        }
        return v;
    }

    public static NutsInstallLogAction parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsInstallLogAction parseLenient(String value, NutsInstallLogAction emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsInstallLogAction parseLenient(String value, NutsInstallLogAction emptyValue, NutsInstallLogAction errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsInstallLogAction.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    @Override
    public String id() {
        return id;
    }
}
