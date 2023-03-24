package net.thevpc.nuts.util;

import net.thevpc.nuts.NOptional;

public enum NQuoteType implements NEnum {
    DOUBLE,
    SIMPLE,
    ANTI;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NQuoteType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NQuoteType> parse(String value) {
        return NEnumUtils.parseEnum(value, NQuoteType.class);
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
