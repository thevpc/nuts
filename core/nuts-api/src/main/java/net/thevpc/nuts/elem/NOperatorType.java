package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NOperatorType implements NEnum {
    BINARY,
    UNARY;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NOperatorType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NOperatorType> parse(String value) {
        return NEnumUtils.parseEnum(value, NOperatorType.class, ev -> {
            switch (ev.getNormalizedValue()) {
                case "BINARY":
                case "BIN":
                    return NOptional.of(NOperatorType.BINARY);
                case "UNARY":
                case "UN":
                    return NOptional.of(NOperatorType.UNARY);
            }
            return null;
        });
    }


    @Override
    public String id() {
        return id();
    }
}
