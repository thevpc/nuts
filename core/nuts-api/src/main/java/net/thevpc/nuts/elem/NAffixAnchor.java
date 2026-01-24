package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

import java.util.function.Function;

public enum NAffixAnchor implements NEnum {
    START,
    PRE_1,
    POST_1,
    PRE_2,
    POST_2,
    PRE_3,
    POST_3,
    PRE_4,
    POST_4,
    PRE_5,
    POST_5,
    POST_6,
    POST_7,
    POST_8,
    POST_9,
    SEP_1,
    SEP_2,
    SEP_3,
    SEP_4,
    END;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NAffixAnchor() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NAffixAnchor> parse(String value) {
        return NEnumUtils.parseEnum(value, NAffixAnchor.class, new Function<NEnumUtils.EnumValue, NOptional<NAffixAnchor>>() {
            @Override
            public NOptional<NAffixAnchor> apply(NEnumUtils.EnumValue enumValue) {
                return null;
            }
        });
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }


}

