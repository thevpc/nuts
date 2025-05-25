package net.thevpc.nuts.util;

public enum NMapSideStrategy implements NEnum{
    NON_NULL,
    NON_BLANK,
    NULL,
    BLANK,
    ANY;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NMapSideStrategy() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NMapSideStrategy> parse(String value) {
        return NEnumUtils.parseEnum(value, NMapSideStrategy.class);
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
