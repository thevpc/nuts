package net.thevpc.nuts.artifact;

import net.thevpc.nuts.cmdline.NArgCompleteFlag;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * NVersionPartType enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NVersionPartType implements NEnum {
    NUMBER,
    QUALIFIER,
    SEPARATOR,
    PREFIX,
    SUFFIX;
    private final String id;
  /**
   * N version part type.
   */
    NVersionPartType() {
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
    public static NOptional<NVersionPartType> parse(String value) {
        return NEnumUtils.parseEnum(value, NVersionPartType.class);
    }
}
