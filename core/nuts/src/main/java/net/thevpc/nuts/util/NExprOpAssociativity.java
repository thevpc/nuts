package net.thevpc.nuts.util;

import net.thevpc.nuts.NOptional;

public enum NExprOpAssociativity implements NEnum {
    LEFT,
    RIGHT;
    private final String id;

    NExprOpAssociativity() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NExprOpAssociativity> parse(String value) {
        return NStringUtils.parseEnum(value, NExprOpAssociativity.class);
    }

    @Override
    public String id() {
        return id;
    }
}
