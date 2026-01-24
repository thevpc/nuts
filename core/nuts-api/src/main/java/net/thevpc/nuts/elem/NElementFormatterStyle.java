package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NElementFormatterStyle implements NEnum {
    COMPACT,
    PRETTY,
    CUSTOM,
    ;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NElementFormatterStyle() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NElementFormatterStyle> parse(String value) {
        return NEnumUtils.parseEnum(value, NElementFormatterStyle.class, s->{
            return null;
        });
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
