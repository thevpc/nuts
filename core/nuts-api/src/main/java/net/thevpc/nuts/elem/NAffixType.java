package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * NAffixType enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
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

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
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
