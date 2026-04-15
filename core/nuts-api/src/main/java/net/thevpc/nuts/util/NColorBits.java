package net.thevpc.nuts.util;

public enum NColorBits implements NEnum {
    BITS_4(4),
    BITS_8(8),
    BITS_16(16),
    BITS_24(24),
    BITS_32(32),
    BITS_64(64);
    private final int bits;
    private final String id;

    NColorBits(int bits) {
        this.bits = bits;
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public int bits() {
        return bits;
    }

    @Override
    public String id() {
        return id;
    }

    public static NOptional<NColorBits> parse(String value) {
        return NEnumUtils.parseEnum(value, NColorBits.class, enumValue -> {
            switch (enumValue.normalizedValue()) {
                case "STANDARD":
                    return NOptional.of(NColorBits.BITS_32);
                case "BITS4":
                    return NOptional.of(NColorBits.BITS_4);
                case "BITS8":
                    return NOptional.of(NColorBits.BITS_8);
                case "BITS16":
                    return NOptional.of(NColorBits.BITS_16);
                case "BITS24":
                    return NOptional.of(NColorBits.BITS_24);
                case "BITS32":
                    return NOptional.of(NColorBits.BITS_32);
                case "BITS64":
                    return NOptional.of(NColorBits.BITS_64);
            }
            return NOptional.ofNamedEmpty(value);
        });
    }
}
