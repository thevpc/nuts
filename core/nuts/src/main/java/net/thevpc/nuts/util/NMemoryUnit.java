package net.thevpc.nuts.util;

import net.thevpc.nuts.NOptional;

import java.util.function.Function;

public enum NMemoryUnit implements NEnum {
    BIT, BYTE, KILO_BYTE, MEGA_BYTE, TERA_BYTE, PETA_BYTE, ZETA_BYTE;
    private String id;

    NMemoryUnit() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    @Override
    public String id() {
        return id;
    }

    public static NOptional<NMemoryUnit> parse(String value) {
        return NStringUtils.parseEnum(value, NMemoryUnit.class, new Function<NStringUtils.EnumValue, NOptional<NMemoryUnit>>() {
            @Override
            public NOptional<NMemoryUnit> apply(NStringUtils.EnumValue enumValue) {
                switch (enumValue.getNormalizedValue()) {
                    case "B":
                    case "O":
                        return NOptional.of(BYTE);
                    case "K":
                    case "KB":
                    case "KO":
                        return NOptional.of(KILO_BYTE);
                    case "M":
                    case "MB":
                    case "MO":
                        return NOptional.of(MEGA_BYTE);
                    case "T":
                    case "TB":
                    case "TO":
                        return NOptional.of(TERA_BYTE);
                    case "P":
                    case "PB":
                    case "PO":
                        return NOptional.of(PETA_BYTE);
                    case "Z":
                    case "ZB":
                    case "ZO":
                        return NOptional.of(ZETA_BYTE);
                }
                return null;
            }
        });
    }
}
