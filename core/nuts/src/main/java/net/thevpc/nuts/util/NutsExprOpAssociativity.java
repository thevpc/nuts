package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsOptional;

public enum NutsExprOpAssociativity implements NutsEnum {
    LEFT,
    RIGHT;
    private final String id;

    NutsExprOpAssociativity() {
        this.id = NutsNameFormat.ID_NAME.formatName(name());
    }

    public static NutsOptional<NutsExprOpAssociativity> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsExprOpAssociativity.class);
    }

    @Override
    public String id() {
        return id;
    }
}
