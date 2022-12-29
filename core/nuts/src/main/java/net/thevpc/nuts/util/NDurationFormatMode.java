package net.thevpc.nuts.util;

import net.thevpc.nuts.NOptional;

public enum NDurationFormatMode implements NEnum {
    DEFAULT,
    FIXED,
    CLOCK;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * private constructor
     */
    NDurationFormatMode() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NDurationFormatMode> parse(String value) {
        return NStringUtils.parseEnum(value, NDurationFormatMode.class);
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
