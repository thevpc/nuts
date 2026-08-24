package net.thevpc.nuts.net;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * NHttpMethod enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NHttpMethod implements NEnum {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    OPTIONS,
    HEAD,
    CONNECT,
    TRACE,
    UNKNOWN;

    private String id;

  /**
   * N http method.
   */
    NHttpMethod() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    @Override
    public String id() {
        return id;
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NHttpMethod> parse(String value) {
        return NEnumUtils.parseEnum(value, NHttpMethod.class);
    }
}
