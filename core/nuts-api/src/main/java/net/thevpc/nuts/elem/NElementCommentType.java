package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NElementCommentType implements NEnum {
    SINGLE_LINE,
    MULTI_LINE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NElementCommentType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NElementCommentType> parse(String value) {
        return NEnumUtils.parseEnum(value, NElementCommentType.class);
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
