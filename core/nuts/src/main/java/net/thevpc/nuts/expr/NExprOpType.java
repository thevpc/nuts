package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

public enum NExprOpType implements NEnum {
    INFIX,
    PREFIX,
    POSTFIX;
    private final String id;

    NExprOpType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NExprOpType> parse(String value) {
        return NEnumUtils.parseEnum(value, NExprOpType.class, s -> {
            switch (s.getNormalizedValue()) {
                case "INFIX":
                    return NOptional.of(INFIX);
                case "POSTFIX_OPERATOR":
                case "POSTFIX_OP":
                case "POSTFIX":
                    return NOptional.of(POSTFIX);
                case "PREFIX_OPERATOR":
                case "PREFIX_OP":
                case "PREFIX":
                    return NOptional.of(PREFIX);
            }
            return null;
        });
    }

    @Override
    public String id() {
        return id;
    }
}
