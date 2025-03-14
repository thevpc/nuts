package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NPathType implements NEnum {
    DIRECTORY,
    NAMED_PIPE,
    CHARACTER,
    SYMBOLIC_LINK,
    BLOCK,
    FILE,
    OTHER,
    NOT_FOUND,
    UNKNOWN
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

