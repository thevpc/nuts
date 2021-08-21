package net.thevpc.nuts;

public enum NutsDesktopEnvironmentFamily implements NutsEnum{
    WINDOWS,
    KDE,
    GNOME,
    UBUNTU,
    UNKNOWN,
    NONE
    ;
    public static NutsDesktopEnvironmentFamily parseLenient(String e) {
        if(e==null){
            return UNKNOWN;
        }
        switch(e.toLowerCase()){
            case "win":
            case "windows":return WINDOWS;
            case "kde":return KDE;
            case "gnome":return GNOME;
            case "unknown":return UNKNOWN;
        }
        return UNKNOWN;
    }

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsDesktopEnvironmentFamily() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    /**
     * lower cased identifier.
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}
