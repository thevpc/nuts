package net.thevpc.nuts.platform;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * NOsServiceType enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NOsServiceType implements NEnum {
    INITD,
    SYSTEMD,
    UNSUPPORTED;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NOsServiceType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NOsServiceType> parse(String value) {
        return NEnumUtils.parseEnum(value, NOsServiceType.class);
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
