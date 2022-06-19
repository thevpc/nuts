package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsOptional;

public enum NutsExprOpType implements NutsEnum {
    INFIX,
    PREFIX,
    POSTFIX;
    private final String id;

    NutsExprOpType() {
        this.id = NutsNameFormat.ID_NAME.formatName(name());
    }

    public static NutsOptional<NutsExprOpType> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsExprOpType.class, s -> {
            switch (s.getNormalizedValue()) {
                case "INFIX":
                    return NutsOptional.of(INFIX);
                case "POSTFIX_OPERATOR":
                case "POSTFIX_OP":
                case "POSTFIX":
                    return NutsOptional.of(POSTFIX);
                case "PREFIX_OPERATOR":
                case "PREFIX_OP":
                case "PREFIX":
                    return NutsOptional.of(PREFIX);
            }
            return null;
        });
    }

    @Override
    public String id() {
        return id;
    }
}
