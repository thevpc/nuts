package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NAffixType implements NEnum {
    LINE_COMMENT,
    BLOC_COMMENT,
    SPACE,
    NEWLINE,
    SEPARATOR,
    ANNOTATION,
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NAffixType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NAffixType> parse(String value) {
        return NEnumUtils.parseEnum(value, NAffixType.class);
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
