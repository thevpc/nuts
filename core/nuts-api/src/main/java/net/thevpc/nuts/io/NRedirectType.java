package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Defines the type of redirection for command input or output.
 *
 * <p>Correspondence with {@link ProcessBuilder.Redirect} is provided for
 * PIPE, INHERIT, and REDIRECT types.
 *
 * <ul>
 *     <li>STREAM - use a custom stream</li>
 *     <li>GRAB_STREAM / GRAB_FILE - capture output</li>
 *     <li>PIPE - redirect to another process</li>
 *     <li>INHERIT - use the JVM process streams</li>
 *     <li>PATH - redirect to a file</li>
 *     <li>NULL - discard output</li>
 * </ul>
 */
public enum NRedirectType implements NEnum {
    /**
     * use JDK redirection
     */
    REDIRECT,
    /**
     * use a custom stream
     */
    STREAM,
    /**
     * capture output as a stream
     */
    GRAB_STREAM,
    /**
     * capture output as a file
     */
    GRAB_FILE,
    /**
     * The type of {@link ProcessBuilder.Redirect#PIPE Redirect.PIPE}.
     */
    PIPE,

    /**
     * The type of {@link ProcessBuilder.Redirect#INHERIT Redirect.INHERIT}.
     */
    INHERIT,
    /**
     * redirect to a file
     */
    PATH,
    /**
     * discard output
     */
    NULL,
    ;


    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NRedirectType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NRedirectType> parse(String value) {
        return NEnumUtils.parseEnum(value, NRedirectType.class);
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
