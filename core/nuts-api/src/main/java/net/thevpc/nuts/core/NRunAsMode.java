package net.thevpc.nuts.core;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * NRunAsMode enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NRunAsMode implements NEnum {
    CURRENT_USER,
    USER,
    ROOT,
    SUDO;
    private final String id;

  /**
   * N run as mode.
   */
    NRunAsMode() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NRunAsMode> parse(String value) {
        return NEnumUtils.parseEnum(value, NRunAsMode.class);
    }

    @Override
    public String id() {
        return id;
    }

}
