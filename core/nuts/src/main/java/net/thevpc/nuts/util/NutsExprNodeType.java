package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsOptional;

public enum NutsExprNodeType implements NutsEnum {
    FUNCTION,
    OPERATOR,
    VARIABLE,
    LITERAL;
    private final String id;

    NutsExprNodeType() {
        this.id = NutsNameFormat.ID_NAME.formatName(name());
    }


    public static NutsOptional<NutsExprNodeType> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsExprNodeType.class, s -> {
            switch (s.getNormalizedValue()) {
                case "VAR":
                case "VARIABLE":
                    return NutsOptional.of(VARIABLE);
                case "FCT":
                case "FUN":
                case "FUNCTION":
                    return NutsOptional.of(FUNCTION);
                case "OP":
                case "OPERATOR":
                    return NutsOptional.of(OPERATOR);
                case "LIT":
                case "LITERAL":
                    return NutsOptional.of(LITERAL);
            }
            return null;
        });
    }

    @Override
    public String id() {
        return id;
    }
}
