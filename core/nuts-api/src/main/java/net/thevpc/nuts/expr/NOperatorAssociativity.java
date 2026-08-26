package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

/**
 * NOperatorAssociativity enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NOperatorAssociativity implements NEnum {
    LEFT,
    RIGHT;
    private final String id;

  /**
   * N operator associativity.
   */
    NOperatorAssociativity() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NOperatorAssociativity> parse(String value) {
        return NEnumUtils.parseEnum(value, NOperatorAssociativity.class);
    }

    @Override
    public String id() {
        return id;
    }
}
