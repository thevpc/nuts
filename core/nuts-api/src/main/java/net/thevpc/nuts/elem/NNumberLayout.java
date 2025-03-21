package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NNumberLayout implements NEnum {
    BINARY,
    DECIMAL,
    OCTAL,
    HEXADECIMAL;;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NNumberLayout() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NNumberLayout> parse(String value) {
        return NEnumUtils.parseEnum(value, NNumberLayout.class, ev -> {
            switch (ev.getNormalizedValue()) {
                case "BINARY":
                case "BIN":
                    return NOptional.of(NNumberLayout.BINARY);
                case "DECIMAL":
                case "DEC":
                    return NOptional.of(NNumberLayout.DECIMAL);
                case "OCTAL":
                case "OCT":
                    return NOptional.of(NNumberLayout.OCTAL);
                case "HEXADECIMAL":
                case "HEXA":
                case "HEX":
                    return NOptional.of(NNumberLayout.HEXADECIMAL);
            }
            return null;
        });
    }


    public int radix() {
        switch (this) {
            case BINARY:
                return 2;
            case OCTAL:
                return 8;
            case DECIMAL:
                return 10;
            case HEXADECIMAL:
                return 16;
        }
        throw new IllegalArgumentException("unexpected NNumberLayout");
    }

    @Override
    public String id() {
        return id();
    }
}
