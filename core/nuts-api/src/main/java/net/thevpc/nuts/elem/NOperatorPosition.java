package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NOperatorPosition implements NEnum {
    INFIX,
    PREFIX,
    SUFFIX,
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NOperatorPosition() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NOperatorPosition> parse(String value) {
        return NEnumUtils.parseEnum(value, NOperatorPosition.class, ev -> {
            switch (ev.getNormalizedValue()) {
                case "IN":
                case "INFIX":
                    return NOptional.of(NOperatorPosition.INFIX);
                case "PRE":
                case "PREFIX":
                    return NOptional.of(NOperatorPosition.PREFIX);
                case "POST":
                case "SUFFIX":
                    return NOptional.of(NOperatorPosition.SUFFIX);
            }
            return null;
        });
    }


    @Override
    public String id() {
        return id;
    }
}
