package net.thevpc.nuts.util;

public enum NDecision implements NEnum{
    ACCEPT,
    DENY,
    ABSTAIN
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NDecision() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NDecision> parse(String value) {
        return NEnumUtils.parseEnum(value, NDecision.class);
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
