package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * NExprCallContextType enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NExprCallContextType implements NEnum {
    FUNCTION,
    CONSTRUCT,
    OPERATOR;
    private final String id;

  /**
   * N expr call context type.
   */
    NExprCallContextType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
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
