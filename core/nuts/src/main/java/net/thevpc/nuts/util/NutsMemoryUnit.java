package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsOptional;

import java.util.function.Function;

public enum NutsMemoryUnit implements NutsEnum {
    BIT, BYTE, KILO_BYTE, MEGA_BYTE, TERA_BYTE, PETA_BYTE, ZETA_BYTE;
    private String id;

    NutsMemoryUnit() {
        this.id = NutsNameFormat.ID_NAME.formatName(name());
    }

    @Override
    public String id() {
        return id;
    }

    public static NutsOptional<NutsMemoryUnit> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsMemoryUnit.class, new Function<NutsStringUtils.EnumValue, NutsOptional<NutsMemoryUnit>>() {
            @Override
            public NutsOptional<NutsMemoryUnit> apply(NutsStringUtils.EnumValue enumValue) {
                switch (enumValue.getNormalizedValue()) {
                    case "B":
                    case "O":
                        return NutsOptional.of(BYTE);
                    case "K":
                    case "KB":
                    case "KO":
                        return NutsOptional.of(KILO_BYTE);
                    case "M":
                    case "MB":
                    case "MO":
                        return NutsOptional.of(MEGA_BYTE);
                    case "T":
                    case "TB":
                    case "TO":
                        return NutsOptional.of(TERA_BYTE);
                    case "P":
                    case "PB":
                    case "PO":
                        return NutsOptional.of(PETA_BYTE);
                    case "Z":
                    case "ZB":
                    case "ZO":
                        return NutsOptional.of(ZETA_BYTE);
                }
                return null;
            }
        });
    }
}
