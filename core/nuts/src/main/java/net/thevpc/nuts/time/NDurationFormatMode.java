package net.thevpc.nuts.time;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

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
        return NEnumUtils.parseEnum(value, NDurationFormatMode.class);
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
