package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

public enum NExprNodeType implements NEnum {
    FUNCTION,
    OPERATOR,
    WORD,
    LITERAL;
    private final String id;

    NExprNodeType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }


    public static NOptional<NExprNodeType> parse(String value) {
        return NEnumUtils.parseEnum(value, NExprNodeType.class, s -> {
            switch (s.getNormalizedValue()) {
                case "VAR":
                case "VARIABLE":
                    return NOptional.of(WORD);
                case "FCT":
                case "FUN":
                case "FUNCTION":
                    return NOptional.of(FUNCTION);
                case "OP":
                case "OPERATOR":
                    return NOptional.of(OPERATOR);
                case "LIT":
                case "LITERAL":
                    return NOptional.of(LITERAL);
            }
            return null;
        });
    }

    @Override
    public String id() {
        return id;
    }
}
