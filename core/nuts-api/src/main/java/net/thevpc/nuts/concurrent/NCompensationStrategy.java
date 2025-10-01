package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * @since 0.8.7
 */
public enum NCompensationStrategy implements NEnum {
    ABORT,                  // stop saga on compensation failure
    IGNORE;                 // continue even if compensation fails

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NCompensationStrategy() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NCompensationStrategy> parse(String value) {
        return NEnumUtils.parseEnum(value, NCompensationStrategy.class);
    }

    @Override
    public String id() {
        return id;
    }
}
