package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

public enum NutsDesktopIntegrationItem implements NutsEnum {
    MENU,
    DESKTOP,
    SHORTCUT;
    private final String id;

    NutsDesktopIntegrationItem() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsDesktopIntegrationItem parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsDesktopIntegrationItem parseLenient(String value, NutsDesktopIntegrationItem emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsDesktopIntegrationItem parseLenient(String value, NutsDesktopIntegrationItem emptyValue, NutsDesktopIntegrationItem errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsDesktopIntegrationItem.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    public static NutsDesktopIntegrationItem parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsDesktopIntegrationItem parse(String value, NutsDesktopIntegrationItem emptyValue, NutsSession session) {
        NutsDesktopIntegrationItem v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v,value,NutsDesktopIntegrationItem.class,session);
        return v;
    }

    @Override
    public String id() {
        return id;
    }

}
