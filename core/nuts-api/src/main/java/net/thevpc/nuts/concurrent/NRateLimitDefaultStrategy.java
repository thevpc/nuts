package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NRateLimitDefaultStrategy implements NEnum {
    BUCKET,
    LEAKY_BUCKET,
    SLIDING_WINDOW,
    FIXED_WINDOW;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NRateLimitDefaultStrategy() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NRateLimitDefaultStrategy> parse(String value) {
        return NEnumUtils.parseEnum(value, NRateLimitDefaultStrategy.class);
    }

    @Override
    public String id() {
        return id;
    }

}
