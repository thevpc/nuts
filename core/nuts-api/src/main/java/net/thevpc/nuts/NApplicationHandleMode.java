package net.thevpc.nuts;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NApplicationHandleMode implements NEnum {
    HANDLE,
    PROPAGATE,
    EXIT,
    NOP;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NApplicationHandleMode() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NApplicationHandleMode> parse(String value) {
        return NEnumUtils.parseEnum(value, NApplicationHandleMode.class);
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
