package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

/**
 * NFixity enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NFixity implements NEnum {
    INFIX,
    PREFIX,
    POSTFIX;
    private final String id;

  /**
   * N fixity.
   */
    NFixity() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NFixity> parse(String value) {
        return NEnumUtils.parseEnum(value, NFixity.class, s -> {
            switch (s.normalizedValue()) {
                case "PRE":
                case "PREFIX_OPERATOR":
                case "PREFIX_OP":
                case "PREFIX":
                    return NOptional.of(PREFIX);

                case "INFIX_OPERATOR":
                case "INFIX":
                case "IN":
                    return NOptional.of(INFIX);


                case "POST":
                case "POSTFIX_OPERATOR":
                case "POSTFIX_OP":
                case "POSTFIX":
                    return NOptional.of(POSTFIX);

            }
            return null;
        });
    }

    @Override
    public String id() {
        return id;
    }
}
