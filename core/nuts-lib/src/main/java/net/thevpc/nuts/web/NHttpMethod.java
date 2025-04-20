package net.thevpc.nuts.web;

import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

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

    NHttpMethod() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    @Override
    public String id() {
        return id;
    }

    public static NOptional<NTerminalMode> parse(String value) {
        return NEnumUtils.parseEnum(value, NTerminalMode.class);
    }
}
