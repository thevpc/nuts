package net.thevpc.nuts;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NExecRedirectType implements NEnum {
    REDIRECT,
    STREAM,
    GRAB_STREAM,
    GRAB_FILE,
    /**
     * The type of {@link ProcessBuilder.Redirect#PIPE Redirect.PIPE}.
     */
    PIPE,

    /**
     * The type of {@link ProcessBuilder.Redirect#INHERIT Redirect.INHERIT}.
     */
    INHERIT,
    PATH,
    NULL,
    ;


    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NExecRedirectType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NExecRedirectType> parse(String value) {
        return NEnumUtils.parseEnum(value, NExecRedirectType.class);
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
