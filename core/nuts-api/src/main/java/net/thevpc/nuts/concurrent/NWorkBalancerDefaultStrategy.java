package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 *
 * @since 0.8.7
 */
public enum NWorkBalancerDefaultStrategy implements NEnum {
    ROUND_ROBIN,
    LEAST_LOAD,
    POWER_OF_TWO_CHOICES;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NWorkBalancerDefaultStrategy() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NWorkBalancerDefaultStrategy> parse(String value) {
        return NEnumUtils.parseEnum(value, NWorkBalancerDefaultStrategy.class);
    }

    @Override
    public String id() {
        return id;
    }

}
