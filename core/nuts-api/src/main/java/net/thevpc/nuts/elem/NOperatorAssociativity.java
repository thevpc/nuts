package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

public enum NOperatorAssociativity implements NEnum {
    LEFT,
    RIGHT;
    private final String id;

    NOperatorAssociativity() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NOperatorAssociativity> parse(String value) {
        return NEnumUtils.parseEnum(value, NOperatorAssociativity.class);
    }

    @Override
    public String id() {
        return id;
    }
}
