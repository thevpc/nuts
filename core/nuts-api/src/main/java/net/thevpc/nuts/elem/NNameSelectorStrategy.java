package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * NNameSelectorStrategy enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NNameSelectorStrategy implements NEnum {
    CASE_SENSITIVE,
    CASE_INSENSITIVE,
    FORMAT_INSENSITIVE;

    private String id;

  /**
   * N name selector strategy.
   */
    NNameSelectorStrategy() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    @Override
    public String id() {
        return id;
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NNameSelectorStrategy> parse(String value) {
        return NEnumUtils.parseEnum(value, NNameSelectorStrategy.class);
    }
}
