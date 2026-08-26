package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * NPathType enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NPathType implements NEnum {
    DIRECTORY,
    NAMED_PIPE,
    CHARACTER_DEVICE,
    SYMBOLIC_LINK,
    BLOCK_DEVICE,
    FILE,
    SOCKET,
    OTHER,
    NOT_FOUND
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NPathType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NPathType> parse(String value) {
        return NEnumUtils.parseEnum(value, NPathType.class);
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

