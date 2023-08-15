package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

public enum NExprOpAssociativity implements NEnum {
    LEFT,
    RIGHT;
    private final String id;

    NExprOpAssociativity() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NExprOpAssociativity> parse(String value) {
        return NEnumUtils.parseEnum(value, NExprOpAssociativity.class);
    }

    @Override
    public String id() {
        return id;
    }
}
