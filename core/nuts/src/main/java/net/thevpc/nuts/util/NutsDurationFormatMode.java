package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsOptional;

public enum NutsDurationFormatMode implements NutsEnum {
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
    NutsDurationFormatMode() {
        this.id = NutsNameFormat.ID_NAME.format(name());
    }

    public static NutsOptional<NutsDurationFormatMode> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsDurationFormatMode.class);
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
