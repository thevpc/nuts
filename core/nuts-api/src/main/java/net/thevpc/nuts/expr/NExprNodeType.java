package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

/**
 * NExprNodeType enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NExprNodeType implements NEnum {
    FUNCTION,
    OPERATOR,
    WORD,
    LITERAL,
    INTERPOLATED_STR,
    IF,
    ;
    private final String id;

  /**
   * N expr node type.
   */
    NExprNodeType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }


    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NExprNodeType> parse(String value) {
        return NEnumUtils.parseEnum(value, NExprNodeType.class, s -> {
            switch (s.normalizedValue()) {
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
                case "INTERPOLATED_STR":
                case "ISTR":
                    return NOptional.of(INTERPOLATED_STR);
                case "IF":
                    return NOptional.of(IF);
            }
            return null;
        });
    }

    @Override
    public String id() {
        return id;
    }
}
