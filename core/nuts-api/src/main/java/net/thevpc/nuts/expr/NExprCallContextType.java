package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NExprCallContextType implements NEnum {
    FUNCTION,
    CONSTRUCT,
    OPERATOR;
    private final String id;

    NExprCallContextType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NExprCallContextType> parse(String value) {
        return NEnumUtils.parseEnum(value, NExprCallContextType.class, s -> {
            switch (s.normalizedValue()) {
                case "FUN":
                case "FUNCTION":
                case "FCT":
                    return NOptional.of(FUNCTION);
                case "NEW":
                case "CONSTRUCT":
                case "CONSTRUCTOR":
                    return NOptional.of(CONSTRUCT);
                case "OP":
                case "OPERATOR":
                    return NOptional.of(OPERATOR);
            }
            return null;
        });
    }

    @Override
    public String id() {
        return id;
    }
}
