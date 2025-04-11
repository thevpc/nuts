package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum SearchExecType implements NEnum {
    EXEC,
    RUNTIME,
    LIB,
    NUTS_APPLICATION,
    PLATFORM_APPLICATION,
    COMPANION,
    EXTENSION
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * private constructor
     */
    SearchExecType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<SearchExecType> parse(String value) {
        return NEnumUtils.parseEnum(value, SearchExecType.class);
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
