package net.thevpc.nuts.util;

public enum NMatchType implements NEnum{
    FULL_MATCH,
    MATCH,
    PARTIAL_MATCH,
    NO_MATCH;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NMatchType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NMatchType> parse(String value) {
        return NEnumUtils.parseEnum(value, NMatchType.class);
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
