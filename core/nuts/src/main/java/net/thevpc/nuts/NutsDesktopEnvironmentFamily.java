package net.thevpc.nuts;

public enum NutsDesktopEnvironmentFamily implements NutsEnum {
    WINDOWS,
    KDE,
    GNOME,
    UBUNTU,
    UNKNOWN,
    NONE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsDesktopEnvironmentFamily() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsDesktopEnvironmentFamily parseLenient(String e) {
        return parseLenient(e, UNKNOWN);
    }

    public static NutsDesktopEnvironmentFamily parseLenient(String e, NutsDesktopEnvironmentFamily emptyOrErrorValue) {
        return parseLenient(e, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsDesktopEnvironmentFamily parseLenient(String e, NutsDesktopEnvironmentFamily emptyValue, NutsDesktopEnvironmentFamily errorValue) {
        if (e == null) {
            e = "";
        } else {
            e = e.toLowerCase().trim();
        }
        switch (e) {
            case "":
                return emptyValue;
            case "win":
            case "windows":
                return WINDOWS;
            case "kde":
                return KDE;
            case "gnome":
                return GNOME;
            case "unknown":
                return UNKNOWN;
        }
        return errorValue;
    }

    public static NutsDesktopEnvironmentFamily parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsDesktopEnvironmentFamily parse(String value, NutsDesktopEnvironmentFamily emptyValue, NutsSession session) {
        NutsDesktopEnvironmentFamily v = parseLenient(value, emptyValue, null);
        if (v == null) {
            if (!NutsBlankable.isBlank(value)) {
                throw new NutsParseEnumException(session, value, NutsDesktopEnvironmentFamily.class);
            }
        }
        return v;
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}
