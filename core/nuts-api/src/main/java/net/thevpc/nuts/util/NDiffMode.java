package net.thevpc.nuts.util;

import net.thevpc.nuts.io.NpsStatus;

public enum NDiffMode implements NEnum{
    ADDED, REMOVED, CHANGED, UNCHANGED;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NDiffMode() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NDiffMode> parse(String value) {
        return NEnumUtils.parseEnum(value, NDiffMode.class);
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
