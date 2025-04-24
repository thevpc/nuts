package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NpsType implements NEnum {
    PROCESS,
    KERNEL_THREAD,
    UNKNOWN
    ;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NpsType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NpsType> parse(String value) {
        return NEnumUtils.parseEnum(value, NpsType.class);
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
